package com.bencode;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * BencodeDecoder decodes bencoded data from input streams.
 * currentCharacter points to current byte read from stream.
 * The data types supported by Bencoding are:  byte strings, integers, lists, and dictionaries.
 * They are represented by generic {@code BencodeValue} class.
 * Each method in the decoder has return-type {@code BencodeValue} since the underlying value can be any of the above types
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

    /**
     * The current bencoded entity is a number (starts with 'i' and ends with 'e')
     * value is either an integer or long
     * @example i3e  ==> 3
     * @return the decoded number wrapped in a generic BencodeValue instance
     * @throws IOException
     * @throws InvalidBencodingException
     */
    private BencodeValue decodeNumber() throws IOException, InvalidBencodingException {

        // current character is 'i' so move forward
        this.forwardPointer();
        String number = "";

        // loop until 'e', characters between 'i' and 'e' is the number so append the characters
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
        // return the number wrapped in a BencodeValue instance
        return new BencodeValue(num);
    }

    /**
     * The current bencoded entity is a list (starts with 'l' and ends with 'e')
     * The list can be a list of string, number, dict, list or any combination of these elements
     * Hence the list is a list of generic BencodeValue elements representing all the above elemnents
     * @example l4:spami45ee ==> ["spam",45]
     * @return the decoded list wrapped in a generic BencodeValue instance
     * @throws IOException
     * @throws InvalidBencodingException
     */
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

    /**
     * The current bencoded entity is a string represented as <length>:<string>
     * @example 4:spam ==> "spam"
     * @return the decoded string wrapped in a generic BencodeValue instance
     * @throws IOException
     * @throws InvalidBencodingException
     */
    private BencodeValue decodeBytes() throws InvalidBencodingException, IOException {
        int length = 0;

        // get length of byte string
        while(Character.isDigit(this.currentCharacter)) {
            length = length * 10 + (currentCharacter - '0');
            this.forwardPointer();
        }

        if(this.currentCharacter != ':')
            throw new InvalidBencodingException("Expected colon (:) got " + this.currentCharacter);

        // read `length` bytes from stream. Preserve data by wrapping byte[] into BencodeValue
        byte[] bytes = new byte[length];
        this.inputStream.read(bytes);
        return new BencodeValue(bytes);
    }

    /**
     * The current bencoded entity is a dictionary (starts with 'd' and ends with 'e')
     * key of a bencoded dictionary is always string, value can be a list, dict, number or string
     * @example d4:spaml1:a1:bee ==> {"spam" => [ "a", "b" ]}
     * @return the decoded dict wrapped in BencodeValue instance
     * @throws IOException
     * @throws InvalidBencodingException
     */
    private BencodeValue decodeDict() throws IOException, InvalidBencodingException {
        // currentCharacter points to 'd' so move forward
        this.forwardPointer();
        BencodeValue dict = null;

        // key is always string, value can be list, dict, number, string so represented by BencodeValue
        HashMap<String, BencodeValue> map = new HashMap<>();
        while(this.currentCharacter != 'e') {
            // key must always be byte string
            BencodeValue key = this.decodeBytes();

            // currentCharacter is pointing to end of previous encoded value so move pointer forward
            this.forwardPointer();

            //  recur for the value
            BencodeValue value = this.decodeUtil();

            map.put(new String((byte[])key.getValue()),value);
            this.forwardPointer();
        }

        // wrap the dict in BencodeValue instance and return
        dict = new BencodeValue(map);
        return dict;
    }
}
