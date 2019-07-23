package com.bencode;

public class InvalidBencodingException extends Exception {
    public InvalidBencodingException(String message) {
        super(message);
    }
}
