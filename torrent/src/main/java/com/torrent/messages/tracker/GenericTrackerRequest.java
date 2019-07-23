package com.torrent.messages.tracker;


import java.nio.ByteBuffer;

public abstract class GenericTrackerRequest extends GenericTrackerMessage {

    public GenericTrackerRequest(int action,int transactionId) {
        super(action,transactionId);
    }

    public abstract ByteBuffer getRequestBuffer();
}
