/*
 *  Copyright (c) Lightstreamer Srl
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.lightstreamer.adapters.remote;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

abstract class RemotingProtocol {
    public static final char SEP= '|';
    
    public static final String VALUE_NULL= "#";
    public static final String VALUE_EMPTY= "$";
    public static final String VALUE_TRUE= "1";
    public static final String VALUE_FALSE= "0";
    
    public static final char TYPE_VOID= 'V';
    public static final char TYPE_STRING= 'S';
    public static final char TYPE_BOOLEAN= 'B';
    public static final char TYPE_INT= 'I';
    public static final char TYPE_LONG= 'L';
    public static final char TYPE_DOUBLE= 'D';
    public static final char TYPE_EXCEPTION= 'E';
    
    ///////////////////////////////////////////////////////////////////////////
    // base encoding/decoding methods



    // old encoding/decoding method, also referred do as
    // ARI "Backward-Compatibility Encoding"

    protected static String encodeStringOld(String string) throws RemotingException {
        if (string == null) return VALUE_NULL;
        if (string.length() == 0) return VALUE_EMPTY;
        
        try {
            return URLEncoder.encode(string, "UTF-8");
        
        } catch (UnsupportedEncodingException e) {
            assert (false);
            throw new RemotingException("Unexpected error while url-encoding string", e);
        } catch (RuntimeException e) {
            throw new RemotingException("Unknown error while url-encoding string", e);
        }
    }

    protected static String decodeStringOld(String string) throws RemotingException {
        if (string.equals(VALUE_NULL)) return null;
        if (string.equals(VALUE_EMPTY)) return "";
        
        try {
            return URLDecoder.decode(string, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            assert (false);
            throw new RemotingException("Unexpected error while url-decoding string", e);
        } catch (RuntimeException e) {
            throw new RemotingException("Unknown error while url-decoding string", e);
        }
    }



    // new encoding/decoding method, also referred do as
    // ARI "Smart Encoding"

    private static final byte[] hex = new byte[16];

    static {
        for (int i = 0; i <= 9; i++) {
            hex[i] = (byte) ('0' + i);
        }
        for (int i = 10; i < 16; i++) {
            hex[i] = (byte) ('A' + (i - 10));
        }
    }

    public static final char CHAR_NULL = VALUE_NULL.charAt(0);
    public static final char CHAR_EMPTY = VALUE_EMPTY.charAt(0);

    private static final boolean isReserved(int b) {
        if ((b == CHAR_NULL) || (b == CHAR_EMPTY)) {
            // characters used also with a special meaning,
            // but that don't affect the parsing of the message;
            // so, percent-encoding them always is not needed,
            // as they are used with special meaning only at value start
            return true;
        }
        return false;
    }

    private static final boolean isSpecial(int b) {
        if ((b == '\r') || (b == '\n') || (b == SEP)) {
            // characters used to delimit messages and message parts;
            // they have to be percent-encoded always
            return true;
        } else if (b == '%') {
            // character used for quoting
            return true;
        } else if (b == '+') {
            // character used in a special way by URLDecoder.decode,
            // if we suppress it from our encoded strings,
            // we can still use URLDecoder.decode for decoding
            return true;
        } else {
            // all other characters, including all non-ascii UTF-8
            // characters, can be transmitted in their UTF-8 format
            return false;
        }
    }

    protected static String encodeString(String str) throws RemotingException {
        if (str == null) return VALUE_NULL;
        if (str.length() == 0) return VALUE_EMPTY;

        // NOTE: the reserved characters (CHAR_NULL and CHAR_EMPTY)
        // have to be percent-encoded only in strings of length 1

        try {
            int specials = 0;
            int len = str.length();
            // preliminary step to determine how much to allocate
            for (int i = 0; i < len; i++) {
                char c = str.charAt(i);
                if (isSpecial(c) || (len == 1 && i == 0 && isReserved(c))) {
                    specials++;
                }
            }

            if (specials > 0) {
                int quotedLength = len + specials * 2;
                char[] quoted = new char[quotedLength];
                int j = 0;

                for (int i = 0; i < len; i++) {
                    char c = str.charAt(i);
                    if (isSpecial(c) || (len == 1 && i == 0 && isReserved(c))) {
                        assert((c & 0x7F) == c);
                        // UTF-8 percent encoding applied only to ascii characters;
                        // the result is ascii, hence compatible with java.lang.String's UTF-16
                        quoted[j++] = '%';
                        quoted[j++] = (char) (hex[(c >> 4) & 0xF]);
                        quoted[j++] = (char) (hex[c & 0xF]);
                    } else {
                        // still UTF-16
                        quoted[j++] = c;
                    }
                }
                assert(j == quotedLength);
                return new String(quoted);
            } else {
                return str;
            }
        } catch (RuntimeException e) {
            throw new RemotingException("Unknown error while percent-encoding string", e);
        }
    }
    
    protected static String decodeString(String str) throws RemotingException {
        // since the new encoding specifications suppress the '+' character
        // and since the URLDecode algorithm supports unencoded characters,
        // we can use the URLDecoder also with the new encoding;
        // we rely on the Proxy Adapter to obey the protocol, so we don't check
        // that indeed str doesn't contain the '+' character
        return decodeStringOld(str);
    }



    // byte array based encoding/decoding method, no longer needed

    protected static String encodeBytesAsString(byte[] bytes) throws RemotingException {
        if (bytes == null) return VALUE_NULL;
        if (bytes.length == 0) return VALUE_EMPTY;

        String equivalentStr = new String(bytes, StandardCharsets.ISO_8859_1);
        return encodeString(equivalentStr);
    }
}
