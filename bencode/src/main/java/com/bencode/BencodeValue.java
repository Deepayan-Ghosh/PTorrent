package com.bencode;

import java.io.InvalidClassException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Wrapper for allowed data types in bencoding. Dictionaries, lists, numbers and strings
 * Instead of string, we use byte[] to preserve raw data which comes in the file.
 */

public class BencodeValue {
    private Object value;

    public BencodeValue(Map<String, BencodeValue> dictionary) {
        this.value = dictionary;
    }

    public BencodeValue(List<BencodeValue> list) {
        this.value = list;
    }

    public BencodeValue(Number numericValue) {
        this.value = numericValue;
    }

    public BencodeValue(byte[] byteArray) {
        this.value = byteArray;
    }


    /**
     * If the current type of value is dictionary, then return value corresponding to key k.
     * Since we store strings as byte[], the string version of key is converted to byte array.
     * @param k the key for which value is to be returned
     * @return the value corresponding to key k
     * @throws InvalidClassException if the current value is not a dictionary
     */
    public BencodeValue get(String k) throws InvalidClassException {
    //    BencodeValue key = new BencodeValue(k.getBytes());
        if(this.value instanceof Map)
            return (BencodeValue) ((Map) this.value).get(k);
        else
            throw new InvalidClassException("Map expected found " + this.value.getClass());
    }

    /**
     * If current value is a list, then return element in list at particular index
     * @param idx index whose value is required
     * @return value present in list at index @param idx
     * @throws InvalidClassException if current value is not of type List
     */
    public BencodeValue get(int idx) throws InvalidClassException {
        if(this.value instanceof List)
            return (BencodeValue)((List) this.value).get(idx);
        else
            throw new InvalidClassException("List expected found " + this.value.getClass());
    }

    public Object getValue() {
        return this.value;
    }

    /**
     * Used to get string representation of the current value. If byte[], then a string is obtained from byte array.
     * For rest types, the respective toString() implementations are called
     * @return string representation of value
     */
    @Override
    public String toString() {
        if(this.value instanceof byte[])
            return new String((byte[]) this.value);
        return this.value.toString();
    }

    /**
     * Compares two BencodeValue objects. If byte[], then compare each element of two arrays for equality.
     * @param o other value with which current value is compared
     * @return true or false denoting equal or not-equal
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BencodeValue other = (BencodeValue) o;
        if(other.value instanceof  byte[])
            return Arrays.equals((byte[])other.value, (byte[])this.value);
        return Objects.equals(value, other.value);
    }

    /**
     * Calculates hash for the current BencodeValue object.
     * If type is byte[], then hash is calculated using each element of array.
     * @return hash value
     */
    @Override
    public int hashCode() {
        if(this.value instanceof byte[])
            return Arrays.hashCode((byte[]) this.value);
        if(this.value instanceof List || this.value instanceof Map || this.value instanceof String)
            return this.value.hashCode();
        return Objects.hash(value);
    }
}
