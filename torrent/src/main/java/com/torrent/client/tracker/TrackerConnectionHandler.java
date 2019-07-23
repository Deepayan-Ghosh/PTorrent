package com.torrent.client.tracker;

import com.bencode.BencodeValue;
import com.torrent.messages.tracker.*;
import com.torrent.utils.PrevRequestAndNextTimeoutWrapper;
import com.torrent.utils.TorrentConstants;
import com.torrent.utils.TorrentContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.UnresolvedAddressException;
import java.util.*;

/**
 * @author Deepayan
 * This class handles both connect requests and announce requests
 * channelHashCodeVsTimeoutLastRequest is of the form
 * { "key1" : <nxtTimeoutExp, currTimeout, channel>,
 *   "key2" : <nxtTimeoutExp, currTimeout, channel>}
 */

public class TrackerConnectionHandler implements Runnable {
    private Logger LOGGER = Logger.getLogger(TrackerAnnounceResponse.class.getName());
    private TorrentContext context;

    private HashSet<URI> trackerURIs;
    private HashMap<Integer, PrevRequestAndNextTimeoutWrapper> channelHashCodeVsTimeoutLastRequest;
    private Selector selector;
    private HashMap<Integer, Set<DatagramChannel>> timeoutVsChannels;

    public TrackerConnectionHandler(TorrentContext context) throws URISyntaxException, IOException {
        this.trackerURIs = new HashSet<>();
        this.context = context;
        this.timeoutVsChannels = new HashMap<>();
        this.timeoutVsChannels.put(0, new HashSet<>());
        this.channelHashCodeVsTimeoutLastRequest = new HashMap<>();

        // get list of all tracker urls
        byte[] announceUrl = (byte[]) ((BencodeValue) this.context.getProperty(TorrentConstants.ANNOUNCE_URL)).getValue();
        this.trackerURIs.add(new URI(new String(announceUrl)));
        ArrayList<BencodeValue> announceList = (ArrayList<BencodeValue>) ((BencodeValue) this.context.getProperty(TorrentConstants.ANNOUNCE_LIST)).getValue();
        for (BencodeValue list : announceList) {
            for (BencodeValue value : (ArrayList<BencodeValue>) list.getValue()) {
                byte[] uri = (byte[]) value.getValue();
                this.trackerURIs.add(new URI(new String(uri)));
            }
        }
        LOGGER.info("Number of trackers = " + this.trackerURIs.size());

        // register channels with selector
        this.selector = Selector.open();
        for (URI uri : this.trackerURIs) {
            DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            int port = uri.getPort();
            if (port == -1)
                port = 6969;

            try {
                channel.connect(new InetSocketAddress(uri.getHost(), port));
                if(!channel.socket().getInetAddress().getHostAddress().equals(InetAddress.getLocalHost())) {
                    channel.register(selector, SelectionKey.OP_READ);
                    this.timeoutVsChannels.get(0).add(channel);
                    this.channelHashCodeVsTimeoutLastRequest.put(channel.hashCode(), new PrevRequestAndNextTimeoutWrapper(null));
                    LOGGER.debug("Connected to url: " + uri.getHost() + " " + port);
                }
            } catch (UnresolvedAddressException e) {
                LOGGER.error("INVALID TRACKER -- Failed to connect to tracker: " + uri.getHost() + ":" + port);
            }
        }
    }

    public void connect() throws IOException {
        LOGGER.debug("Connect(): Start");

        int currSecond = 0, noOfReadyChannels;
        do {
            sendRequestToAllChannelsForCurrSecond(currSecond);

            // all channels for current second have been processed so remove
            timeoutVsChannels.remove(currSecond);


            // wait for 1 second for responses
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            noOfReadyChannels = selector.selectNow();
            currSecond++;

            LOGGER.debug("Current Second ::: " +currSecond + "........... Number of ready channels = " + noOfReadyChannels);
            // parse responses
            if (noOfReadyChannels != 0) {
                receiveAndParseResponses(currSecond);
                currSecond++;
            }
        } while (!timeoutVsChannels.isEmpty());

        LOGGER.debug("File ::: " + context.getProperty("fileName") + "......... " + context.getProperty(TorrentConstants.PEER_ADDRESSES));
        LOGGER.debug("Connect(): End");
    }

    private void receiveAndParseResponses(int currSecond) throws IOException {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> itr = selectedKeys.iterator();
        GenericTrackerRequest nextRequest;
        while (itr.hasNext()) {
            SelectionKey selectedKey = itr.next();
            DatagramChannel selectedChannel = (DatagramChannel) selectedKey.channel();
            if (selectedKey.isReadable()) {
                LOGGER.debug("Selected channel ::: " + selectedChannel.socket().getInetAddress());

                // get the last lastRequest, needed for getting transaction id used in validation
                GenericTrackerRequest lastRequest =  channelHashCodeVsTimeoutLastRequest.get(
                        selectedChannel.hashCode()).getLastRequest();
                int currTimeOutForSelectedChannel = channelHashCodeVsTimeoutLastRequest.get(
                        selectedChannel.hashCode()).getCurrentTimeout();

                // get response
                GenericTrackerResponse response = readResponse(selectedChannel, lastRequest);

                // parse and validate response
                if(response != null) {
                    if (response.getActionCode() == TorrentConstants.CONNECT_ACTION) {
                        nextRequest = new TrackerAnnounceRequest(((TrackerConnectResponse) response).getConnectionId(), (short) selectedChannel.socket().getPort(), context);
                        channelHashCodeVsTimeoutLastRequest.get(selectedChannel.hashCode()).setLastRequest(nextRequest);
                        addChannelToNthSecondWaitList(currSecond + 1, selectedChannel);
                    } else if (response.getActionCode() == TorrentConstants.ANNOUNCE_ACTION) {
                        ((HashSet<InetSocketAddress>) context.getProperty(TorrentConstants.PEER_ADDRESSES)).addAll(((TrackerAnnounceResponse) response).getPeerAddresses());
                    }
                    LOGGER.debug("Received " + response.getClass().getSimpleName() + " response from: " + selectedChannel.socket().getInetAddress());
                }

                this.timeoutVsChannels.get(currTimeOutForSelectedChannel).remove(selectedChannel);
            }
            itr.remove();
        }
    }

    /**
     * For currSecond, selects all the channels to which requests are to be sent
     * If timeout is within limit, then
     *      get the last sent request which, if present, denotes that this channel has been processed once, or it is an announce request
     *      If absent, then this channel is being processed for first time and first request is connect request.
     * @param currSecond
     * @throws IOException
     */
    private void sendRequestToAllChannelsForCurrSecond(int currSecond) throws IOException {
        GenericTrackerRequest message;
        Set<DatagramChannel> channelsForCurrSecond = timeoutVsChannels.get(currSecond);
        PrevRequestAndNextTimeoutWrapper entry;

        if(channelsForCurrSecond!=null && !channelsForCurrSecond.isEmpty()) {
            LOGGER.debug("Current time ::: " + currSecond + ", size="+channelsForCurrSecond.size());

            for (DatagramChannel eachChannel : channelsForCurrSecond) {
                    int interval = channelHashCodeVsTimeoutLastRequest.get(eachChannel.hashCode()).getTimeout();
                    if (interval <= TorrentConstants.TIMEOUT_LIMIT) {
                        entry = channelHashCodeVsTimeoutLastRequest.get(eachChannel.hashCode());
                        message = entry.getLastRequest();

                    /*
                    if request is already wrapped with channel, then this is retransmission or announce request
                    else, this is first request and hence it is connect request
                    */
                    if (message == null) {
                        message = new TrackerConnectRequest();
                        entry.setLastRequest(message);
                    }

                    entry.updateTimeout();
                    entry.setCurrentTimeout(currSecond + interval);


                    LOGGER.debug("Sending request for channel ::: " + eachChannel.socket().getInetAddress() + ", request ::: " + message);
                    eachChannel.write(message.getRequestBuffer());

                    /*
                    provide entries in maps : these channels will have timeout at (current time + interval)
                    {@code message} will become lastRequest when these channels are again processed
                    */
                    addChannelToNthSecondWaitList(currSecond + interval, eachChannel);
                }
            }
        }
    }

    private void addChannelToNthSecondWaitList(int nthSecond, DatagramChannel channel) {
        timeoutVsChannels.putIfAbsent(nthSecond, new HashSet<>());
        timeoutVsChannels.get(nthSecond).add(channel);
    }

    private GenericTrackerResponse readResponse(DatagramChannel selectedChannel, GenericTrackerRequest lastRequest) throws IOException {
        try {
            ByteBuffer receiveBuffer = ByteBuffer.allocate(2048);
            selectedChannel.read(receiveBuffer);
            receiveBuffer.flip();
            return GenericTrackerResponse.parseResponseUtil(receiveBuffer, lastRequest);
        } catch (PortUnreachableException e) {
            LOGGER.error("Could not read from selected channel ::: address=["+selectedChannel.socket().getRemoteSocketAddress()+"]", e);
        }
        return null;
    }

    @Override
    public void run() {
        LOGGER.info("Inside thread: " + Thread.currentThread().getName());
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
