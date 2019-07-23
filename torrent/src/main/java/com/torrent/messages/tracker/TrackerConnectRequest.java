package com.torrent.messages.tracker;

import com.torrent.utils.TorrentConstants;
import com.torrent.utils.TorrentUtils;

import java.nio.ByteBuffer;

public class TrackerConnectRequest extends GenericTrackerRequest {
    private long protocolId;

    public TrackerConnectRequest() {
        super(TorrentConstants.CONNECT_ACTION, TorrentUtils.getTransactionId32());
        this.protocolId = Long.parseLong(TorrentConstants.DEFAULT_PROTOCOL_ID, 16);
    }

    @Override
    public ByteBuffer getRequestBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(0,this.protocolId);
        buffer.putInt(8,this.action);
        buffer.putInt(12, this.transactionId);
        return buffer;
    }

    @Override
    public String toString() {
        return "TrackerConnectRequest{" +
                "protocolId=" + protocolId +
                ", action=" + action +
                ", transactionId=" + transactionId +
                '}';
    }
}
