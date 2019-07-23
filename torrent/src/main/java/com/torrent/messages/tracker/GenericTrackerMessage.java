package com.torrent.messages.tracker;

public abstract class GenericTrackerMessage {
    protected int action, transactionId;

    protected GenericTrackerMessage(int action, int transactionId) {
        this.action = action;
        this.transactionId = transactionId;
    }


    public int getActionCode() {
        return action;
    }

    public void setActionCode(int action) {
        this.action = action;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}
