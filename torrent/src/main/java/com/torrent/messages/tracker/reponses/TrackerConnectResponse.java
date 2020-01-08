package com.torrent.messages.tracker.reponses;

import com.torrent.utils.TorrentConstants;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

public class TrackerConnectResponse extends GenericTrackerResponse {
    private Logger LOGGER = Logger.getLogger(TrackerConnectResponse.class.getName());
    private long connectionId;

    public TrackerConnectResponse() {}

    public TrackerConnectResponse(int action, int transactionId, long connectionId) {
        super(action, transactionId);
        this.connectionId = connectionId;
    }

    @Override
    public void parseResponse(ByteBuffer buffer) {
        this.action = buffer.getInt();
        this.transactionId = buffer.getInt(4);
        this.connectionId = buffer.getLong(8);
    }

    public static boolean validateResponse(final Object ... validationParams) {
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
        return (actionCode == TorrentConstants.CONNECT_ACTION);
    }

    private static boolean validateResponseLength(int length) {
        return (length >= 16);
    }

    public long getConnectionId() {
        return connectionId;
    }

    @Override
    public String toString() {
        return "TrackerConnectResponse{" +
                "connectionId=" + connectionId +
                ", action=" + action +
                ", transactionId=" + transactionId +
                '}';
    }
}
