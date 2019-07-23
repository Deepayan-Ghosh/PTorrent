package com.torrent.utils;

import com.torrent.messages.tracker.GenericTrackerRequest;

public class PrevRequestAndNextTimeoutWrapper {
    private GenericTrackerRequest lastRequest;
    private int timeoutExponent;
    private int currentTimeout;

    public int getCurrentTimeout() {
        return currentTimeout;
    }

    public void setCurrentTimeout(int currentTimeout) {
        this.currentTimeout = currentTimeout;
    }

    public PrevRequestAndNextTimeoutWrapper(GenericTrackerRequest lastRequest) {
        this.lastRequest = lastRequest;
        this.timeoutExponent = 0;
        this.currentTimeout = 0;

    }
    public GenericTrackerRequest getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(GenericTrackerRequest lastRequest) {
        if(this.lastRequest!=null && !lastRequest.getClass().equals(this.lastRequest.getClass())) {
            this.timeoutExponent = 0;
        }
        this.lastRequest = lastRequest;
    }

    public int getTimeoutExponent() {
        return timeoutExponent;
    }

    public void setTimeoutExponent(int timeoutExponent) {
        this.timeoutExponent = timeoutExponent;
    }

    public int getTimeout() {
        return (int) (15 * Math.pow(2,timeoutExponent));
    }

    public void updateTimeout() {
        this.timeoutExponent ++;
    }
}
