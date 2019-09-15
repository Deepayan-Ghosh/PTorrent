package com.torrent.messages;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public interface GenericResponse {
    void parseResponse(ByteBuffer data) throws UnknownHostException;
}
