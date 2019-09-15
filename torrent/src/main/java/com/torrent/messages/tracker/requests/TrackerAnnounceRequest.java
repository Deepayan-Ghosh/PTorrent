package com.torrent.messages.tracker.requests;

import com.bencode.BencodeEncoder;
import com.bencode.BencodeValue;
import com.torrent.utils.TorrentConstants;
import com.torrent.utils.TorrentContext;
import com.torrent.utils.TorrentUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Logger;

public class TrackerAnnounceRequest extends GenericTrackerRequest {
    private static final Logger LOGGER = Logger.getLogger(TrackerAnnounceRequest.class.getName());
    private TorrentContext context;

    private long connectionId;  // 8 bytes
    // action and transactionId from super class 4 bytes each
    private byte[] infoHash;    // 20 bytes
    private byte[] peerId;      // 20 bytes
    private long downloaded;    // 8 bytes
    private long left;          // 8 bytes
    private long uploaded;      // 8 bytes
    private int event;          // 4 bytes
    private int ip;             // 4 bytes
    private byte[] key;         // 4 bytes
    private int numWant;        // 4 bytes
    private short port;         // 2 bytes

    public TrackerAnnounceRequest(long connectionId, short port, TorrentContext ctxt) {
        super(TorrentConstants.ANNOUNCE_ACTION, TorrentUtils.getTransactionId32());
        this.context = ctxt;

        this.connectionId = connectionId;
        this.downloaded = 0;
        this.uploaded = 0;
        this.event = 0;
        this.ip = 0;
        this.key = TorrentUtils.getRandomBytes(4);
        this.numWant = -1;
        this.port = port;

        BencodeValue info = (BencodeValue) this.context.getProperty(TorrentConstants.INFO);
        try {
            this.infoHash = calculateInfoHash(info);
            this.peerId = calculatePeerId();
            this.left = calculateLeftField(info);

        } catch (IOException e) {
            LOGGER.severe("Error while calculating info-hash");
            e.printStackTrace();
        }
    }

    private byte[] calculatePeerId() {
        //  Azureus-style
        ByteBuffer peerId = ByteBuffer.allocate(20);
        String prefix = "-AT0001-";
        peerId.put(prefix.getBytes());
        byte[] randomNumber = TorrentUtils.getRandomBytes(20 - prefix.length());
        int idx=8;
        for(byte b: randomNumber)
            peerId.put(idx++, b);
        return peerId.array();
    }

    private long calculateLeftField(BencodeValue info) throws InvalidClassException {
        List<BencodeValue> files = (List<BencodeValue>) info.get("files").getValue();
        long left = 0;
        if(files == null)
            left = (long) info.get("length").getValue();
        else {
            for(BencodeValue value: files) {
                left = left + Long.valueOf(value.get("length").getValue().toString());
            }
        }
        return left;
    }

    private byte[] calculateInfoHash(BencodeValue info) throws IOException {

        ByteArrayOutputStream infoStream = new ByteArrayOutputStream();
        new BencodeEncoder(infoStream).bencode(info);
        return TorrentUtils.getSHA1(infoStream.toByteArray());
    }

    @Override
    public ByteBuffer getRequestBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(98);
        buffer.putLong(0,this.connectionId);
        buffer.putInt(8,this.action);
        buffer.putInt(12,this.transactionId);
        int idx = 16;
        for(byte b: infoHash)
            buffer.put(idx++, b);
        for(byte b: peerId)
            buffer.put(idx++, b);

        buffer.putLong(56, this.downloaded);
        buffer.putLong(64, this.left);
        buffer.putLong(72, this.uploaded);
        buffer.putInt(80, this.event);
        buffer.putInt(84, this.ip);
        idx = 88;
        for(byte b: this.key)
            buffer.put(idx++, b);
        buffer.putInt(92,this.numWant);
        buffer.putShort(96,this.port);
        return buffer;
    }

    public byte[] getPeerId() {
        return peerId;
    }

    @Override
    public String toString() {
        return "TrackerAnnounceRequest{" +
                "connectionId=" + connectionId +
                ", action=" + action +
                ", transactionId=" + transactionId +
                ", infoHash='" + infoHash + '\'' +
                ", peerId='" + peerId + '\'' +
                ", downloaded=" + downloaded +
                ", left=" + left +
                ", uploaded=" + uploaded +
                ", event=" + event +
                ", ip=" + ip +
                ", key=" + key +
                ", numWant=" + numWant +
                ", port=" + port +
                '}';
    }
}
