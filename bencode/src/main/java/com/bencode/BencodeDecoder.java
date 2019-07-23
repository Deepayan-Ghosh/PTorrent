package com.bencode;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * BencodeDecoder can decode bencoded data from input streams.
 * currentCharacter points to current byte read from stream.
 */

public class BencodeDecoder {
    private Logger LOGGER = Logger.getLogger(BencodeDecoder.class.getName());;
    private InputStream inputStream;
    private char currentCharacter;

    public BencodeDecoder(InputStream stream) {
        this.inputStream = stream;
    }

    /**
     * reads one byte from the stream and makes currentCharacter point to that byte;
     * @throws IOException in case of error while reading from stream
     */
    public void forwardPointer() throws IOException {
        int nextByte = this.inputStream.read();
        if(nextByte == -1)
            currentCharacter = Character.MIN_VALUE;
        currentCharacter = (char) nextByte;
    }

    /**
     * Entry point for decoding the bencoded data stream. Initially, currentCharacter points to nothing.
     * this.forwardPointer() reads one byte and moves currentCharacter to point to the read byte.
     * @return decoded bencoded data
     */
    public BencodeValue decode() throws InvalidBencodingException, IOException {
        LOGGER.info("Decoding start");
        this.forwardPointer();
        LOGGER.info("Decoding end");
        return this.decodeUtil();
    }

    /**
     * 'l' denotes current value is List, 'd' denotes current value is Dictionary
     * 'i' denotes integer or number. A digit means current value is string that is byte[].
     * @return Decoded data
     * @throws IOException if there is error reading from stream, thrown by forwardPointer
     * @throws InvalidBencodingException occurs due to improperly bencoded data
     */
    public BencodeValue decodeUtil() throws IOException, InvalidBencodingException {
        if (this.currentCharacter != Character.MIN_VALUE) {
            if(this.currentCharacter == 'd')
                return this.decodeDict();
            else if(this.currentCharacter == 'l')
                return this.decodeList();
            else if(this.currentCharacter == 'i')
                return this.decodeNumber();
            else if(Character.isDigit(this.currentCharacter)) {
               return this.decodeBytes();
            } else {
                throw new InvalidBencodingException("Unknown character used for encoding");
            }
        }
        return null;
    }

    private BencodeValue decodeNumber() throws IOException, InvalidBencodingException {
        this.forwardPointer();
        String number = "";
        while(this.currentCharacter != 'e') {
            number = number + this.currentCharacter;
            this.forwardPointer();
        }
        Number num = null;

        // allowed number types are integral values that is integer or long. First try to convert to Integer
        // if not possible, causes exception then tries to convert to Long. If still not possible and causes exception,
        // then either the number is too large or is not a number
        try {
            num = Integer.parseInt(number);
        } catch(NumberFormatException e) {
            try {
                num = Long.parseLong(number);
            } catch (NumberFormatException ex) {
                throw new InvalidBencodingException("Valued encoded with 'i' and 'e' is not a number");
            }
        }
        return new BencodeValue(num);
    }

    private BencodeValue decodeList() throws IOException, InvalidBencodingException {
        // currentCharacter points to 'l' so move it forward
        this.forwardPointer();

        ArrayList<BencodeValue> list = new ArrayList<>();
        while(this.currentCharacter != 'e') {
            BencodeValue element = this.decodeUtil();
            list.add(element);
            this.forwardPointer();
        }
        return new BencodeValue(list);
    }

    private BencodeValue decodeBytes() throws InvalidBencodingException, IOException {
        int length = 0;

        // get length of byte string
        while(Character.isDigit(this.currentCharacter)) {
            length = length * 10 + (currentCharacter - '0');
            this.forwardPointer();
        }

        if(this.currentCharacter != ':')
            throw new InvalidBencodingException("Expected colon (:) got " + this.currentCharacter);

        // read length bytes from stream. Preserve data by wrapping byte[] into BencodeValue
        byte[] bytes = new byte[length];
        this.inputStream.read(bytes);
        return new BencodeValue(bytes);
    }

    private BencodeValue decodeDict() throws IOException, InvalidBencodingException {
        // currentCharacter points to 'd' so move forward
        this.forwardPointer();
        BencodeValue dict = null;
        HashMap<String, BencodeValue> map = new HashMap<>();
        while(this.currentCharacter != 'e') {
            // key must always be byte string
            BencodeValue key = this.decodeBytes();

            // currentCharacter is pointing to end of previous encoded value so move pointer forward
            this.forwardPointer();

            BencodeValue value = this.decodeUtil();

            map.put(new String((byte[])key.getValue()),value);
            this.forwardPointer();
        }
        dict = new BencodeValue(map);
        return dict;
    }
}
