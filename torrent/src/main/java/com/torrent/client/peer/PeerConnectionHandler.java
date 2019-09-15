package com.torrent.client.peer;

import com.torrent.utils.BlockingSet;
import com.torrent.utils.TorrentConstants;
import com.torrent.utils.TorrentContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

public class PeerConnectionHandler {
    private TorrentContext context;
    private BlockingSet<InetSocketAddress> peerAddresses;
    private Future<?> isTrackerComplete;

    public PeerConnectionHandler(TorrentContext context, Future<?> isTrackerComplete) {
        this.context = context;
        this.peerAddresses = (BlockingSet<InetSocketAddress>) this.context.getProperty(TorrentConstants.PEER_ID);
        this.isTrackerComplete = isTrackerComplete;
    }


    public void connectAndStartDownloadFromPeers() {
        establishConnections();
    }

    private void establishConnections() {
        InetSocketAddress addr;
        try {
            do {
                SocketChannel channel = SocketChannel.open();
                addr = this.peerAddresses.poll(1000);
                channel.connect(addr);
            } while (!this.isTrackerComplete.isDone() && !this.peerAddresses.isEmpty());
        } catch (IOException e) {

        }
    }
}
