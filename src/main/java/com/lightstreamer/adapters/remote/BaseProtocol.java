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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

class BaseProtocol extends RemotingProtocol {

    public static final String METHOD_KEEPALIVE= "KEEPALIVE";
    public static final String METHOD_REMOTE_CREDENTIALS= "RAC";
    public static final String METHOD_CLOSE= "CLOSE";
    public static final String METHOD_FAILURE = "FAL";

    public static final String KEY_CLOSE_REASON = "reason";

    public static final String AUTH_REQUEST_ID = "1";
    public static final String CLOSE_REQUEST_ID = "0";
    public static final String FAILURE_REQUEST_ID = "2"; // unused by the Server
    public static final long FIRST_REMOTE_REQUEST_ID = 100;

    // ////////////////////////////////////////////////////////////////////////
    // REMOTE CREDENTIALS

    public static String writeRemoteCredentials(Map<String,String> arguments) throws RemotingException {
        // protocol version 1.8.2 and above
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_REMOTE_CREDENTIALS);

        Iterator<Map.Entry<String,String>> iter = arguments.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,String> entry = iter.next();
            sb.append(SEP);
            sb.append(TYPE_STRING);
            sb.append(SEP);
            sb.append(encodeStringOld(entry.getKey()));
            sb.append(SEP);
            sb.append(TYPE_STRING);
            sb.append(SEP);
            sb.append(encodeStringOld(entry.getValue()));
        }

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // CLOSE

    public static Map<String,String> readClose(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);
        String msg = null;
        RemotingException re2 = null;
        
        Map<String,String> parameters = new HashMap<String,String>();

        String typ = null;
        while (tokenizer.hasMoreTokens()) {
            String headerName = "";
            String headerValue = "";

            typ = tokenizer.nextToken();

            String val; // declared here to avoid JVM bug JDK-8067429
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    val = tokenizer.nextToken();
                    headerName = decodeStringOld(val);
                    break;

                default:
                    msg = "Unknown type '" + typ + "' found while parsing a " + METHOD_CLOSE + " request";
            }

            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    val = tokenizer.nextToken();
                    headerValue = decodeStringOld(val);
                    break;

                default:
                    msg = "Unknown type '" + typ + "' found while parsing a " + METHOD_CLOSE + " request";
            }
            
            if (msg != null) {
                re2 = new RemotingException(msg);
                throw re2;
            } else {
                parameters.put(headerName, headerValue);
            }
        }

        return parameters;
    }

    // ////////////////////////////////////////////////////////////////////////
    // FAILURE

    public static String writeFailure(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_FAILURE);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));

        return sb.toString();
    }

    public static String writeFailure() {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_FAILURE);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        sb.append(SEP);
        sb.append(VALUE_NULL);

        return sb.toString();
    }

}