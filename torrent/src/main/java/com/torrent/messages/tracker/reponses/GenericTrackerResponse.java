package com.torrent.messages.tracker.reponses;

import com.torrent.messages.GenericResponse;
import com.torrent.messages.tracker.GenericTrackerMessage;
import com.torrent.messages.tracker.requests.GenericTrackerRequest;
import com.torrent.utils.TorrentConstants;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public abstract class GenericTrackerResponse extends GenericTrackerMessage implements GenericResponse {

    public GenericTrackerResponse() {
        super(0,0);
    }

    protected GenericTrackerResponse(int action, int transactionId) {
        super(action, transactionId);
    }

    public final static GenericTrackerResponse parseResponseUtil(ByteBuffer responseInBytes, GenericTrackerRequest lastRequest) throws UnknownHostException {
        GenericTrackerResponse response = null;
        if(responseInBytes != null && responseInBytes.array().length > 0) {

            int expectedActionCode = lastRequest.getActionCode();
            int expectedTransactionId = lastRequest.getTransactionId();
            boolean validResponse = false;

            if (expectedActionCode == TorrentConstants.CONNECT_ACTION) {
                validResponse = TrackerConnectResponse.validateResponse(responseInBytes, expectedTransactionId);

                if(validResponse) {
                    responseInBytes.rewind();
                    response = new TrackerConnectResponse();
                }
            } else if (expectedActionCode == TorrentConstants.ANNOUNCE_ACTION) {
                validResponse = TrackerAnnounceResponse.validateResponse(responseInBytes, expectedTransactionId);

                if(validResponse) {
                    responseInBytes.rewind();
                    response = new TrackerAnnounceResponse();
                }
            }

            if(validResponse) {
                response.parseResponse(responseInBytes);
            }
        }
        return response;
    }
}
