package com.torrent.messages.tracker;

import com.torrent.utils.TorrentConstants;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TrackerAnnounceResponse extends GenericTrackerResponse {
    private int interval;
    private int leechers;
    private int seeders;
    private List<InetSocketAddress> peerAddresses;

    public TrackerAnnounceResponse() {
        super();
        this.interval = 0;
        this.leechers = 0;
        this.seeders = 0;
        this.peerAddresses = new ArrayList<>();
    }

    public TrackerAnnounceResponse(int action, int transactionId) {
        super(action, transactionId);
    }

    public TrackerAnnounceResponse(int action, int transactionId, int interval, int leechers, int seeders, List<InetSocketAddress> peerAddresses) {
        super(action, transactionId);
        this.interval = interval;
        this.leechers = leechers;
        this.seeders = seeders;
        this.peerAddresses = peerAddresses;
    }

    public static boolean validateResponse(Object ... validationParams) {
        ByteBuffer responseInBytes = (ByteBuffer) validationParams[0];
        int expectedTransactionId = Integer.parseInt(validationParams[1].toString());
        if(responseInBytes != null) {
            // validation 1
            boolean hasValidLength = validateResponseLength(responseInBytes.array().length);
            boolean hasValidActionCode = false, hasValidTransactionId = false;

            if(hasValidLength) {
                // validation 2
                hasValidActionCode = validateActionCode(responseInBytes.getInt());

                // validation 3
                hasValidTransactionId = validateTransactionId(expectedTransactionId, responseInBytes.getInt(4));
            }
            return (hasValidLength && hasValidActionCode && hasValidTransactionId);
        }
        return false;
    }

    private static boolean validateTransactionId(int expectedTransactionId, int actualTransactionId) {
        return (expectedTransactionId == actualTransactionId);
    }

    private static boolean validateActionCode(int actionCode) {
        return (actionCode == TorrentConstants.ANNOUNCE_ACTION);
    }

    private static boolean validateResponseLength(int length) {
        return (length >= 20);
    }

    @Override
    public void parseResponse(ByteBuffer buffer) throws UnknownHostException {
        this.action = buffer.getInt();
        this.transactionId = buffer.getInt();
        this.interval = buffer.getInt();
        this.leechers = buffer.getInt();
        this.seeders = buffer.getInt();

        byte[] ip = new byte[4];
        int numberOfPeers = (buffer.limit() - 20)/6;
        while(numberOfPeers > 0) {
            buffer.get(ip);
            this.peerAddresses.add(new InetSocketAddress(InetAddress.getByAddress(ip), Short.toUnsignedInt(buffer.getShort())));
            numberOfPeers --;
        }
    }

    public static TrackerAnnounceResponse getResponseArrayFromByteArray(byte[] response) throws UnknownHostException {
        ByteBuffer buffer = ByteBuffer.wrap(response);
        int action = buffer.getInt();
        int transactionId = buffer.getInt();
        int interval = buffer.getInt();
        int leechers = buffer.getInt();
        int seeders = buffer.getInt();

        byte[] ip = new byte[4];
        int numberOfPeers = (response.length - 20)/6;
        ArrayList<InetSocketAddress> ips = new ArrayList<>();
        while(numberOfPeers > 0) {
            buffer.get(ip);
            ips.add(new InetSocketAddress(InetAddress.getByAddress(ip), Short.toUnsignedInt(buffer.getShort())));
            numberOfPeers --;
        }

        return new TrackerAnnounceResponse(action, transactionId, interval, leechers, seeders, ips);
    }

    @Override
    public String toString() {
        return "TrackerAnnounceResponse{" +
                "action=" + action +
                ", transactionId=" + transactionId +
                ", interval=" + interval +
                ", leechers=" + leechers +
                ", seeders=" + seeders +
                ", peerAddresses=" + peerAddresses +
                '}';
    }

    public int getLeechers() {
        return leechers;
    }

    public int getSeeders() {
        return seeders;
    }

    public List<InetSocketAddress> getPeerAddresses() {
        return peerAddresses;
    }
}
