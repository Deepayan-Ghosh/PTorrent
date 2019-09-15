package com.torrent.messages.tracker.requests;


import com.torrent.messages.GenericRequest;
import com.torrent.messages.tracker.GenericTrackerMessage;

public abstract class GenericTrackerRequest extends GenericTrackerMessage implements GenericRequest {

    public GenericTrackerRequest(int action,int transactionId) {
        super(action,transactionId);
    }
}
