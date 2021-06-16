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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

class MetadataProviderProtocol extends BaseProtocol {

    public static final char TYPE_MODES = 'M';
    public static final char TYPE_MODE_RAW = 'R';
    public static final char TYPE_MODE_MERGE = 'M';
    public static final char TYPE_MODE_DISTINCT = 'D';
    public static final char TYPE_MODE_COMMAND = 'C';

    public static final char TYPE_MPN_PLATFORM = 'P';
    public static final char TYPE_MPN_PLATFORM_APPLE = 'A';
    public static final char TYPE_MPN_PLATFORM_GOOGLE = 'G';

    public static final char SUBTYPE_METADATAPROVIDER_EXCEPTION = 'M';
    public static final char SUBTYPE_ACCESS_EXCEPTION = 'A';
    public static final char SUBTYPE_CREDITS_EXCEPTION = 'C';
    public static final char SUBTYPE_CONFLICTING_SESSION_EXCEPTION = 'X';
    public static final char SUBTYPE_ITEMS_EXCEPTION = 'I';
    public static final char SUBTYPE_SCHEMA_EXCEPTION = 'S';
    public static final char SUBTYPE_NOTIFICATION_EXCEPTION = 'N';

    public static final String METHOD_METADATA_INIT = "MPI";
    public static final String METHOD_GET_ITEM_DATA = "GIT";
    public static final String METHOD_NOTIFY_USER = "NUS";
    public static final String METHOD_NOTIFY_USER_AUTH = "NUA";
    public static final String METHOD_GET_SCHEMA = "GSC";
    public static final String METHOD_GET_ITEMS = "GIS";
    public static final String METHOD_GET_USER_ITEM_DATA = "GUI";
    public static final String METHOD_NOTIFY_USER_MESSAGE = "NUM";
    public static final String METHOD_NOTIFY_NEW_SESSION = "NNS";
    public static final String METHOD_NOTIFY_SESSION_CLOSE = "NSC";
    public static final String METHOD_NOTIFY_NEW_TABLES = "NNT";
    public static final String METHOD_NOTIFY_TABLES_CLOSE = "NTC";
    public static final String METHOD_NOTIFY_MPN_DEVICE_ACCESS = "MDA";
    public static final String METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION = "MSA";
    public static final String METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE = "MDC";

    // ////////////////////////////////////////////////////////////////////////
    // REMOTE INIT

    public static Map<String,String> readInit(String request) throws RemotingException {
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
                    headerName = decodeString(val);
                    break;

                default:
                    msg = "Unknown type '" + typ + "' found while parsing a " + METHOD_METADATA_INIT + " request";
            }

            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    val = tokenizer.nextToken();
                    headerValue = decodeString(val);
                    break;

                default:
                    msg = "Unknown type '" + typ + "' found while parsing a " + METHOD_METADATA_INIT + " request";
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

    public static String writeInit(Map<String,String> arguments) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_METADATA_INIT);

        if (arguments != null) {
            // protocol version 1.8.1 and above
            assert (! arguments.isEmpty());
            Iterator<Map.Entry<String,String>> iter = arguments.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String,String> entry = iter.next();
                sb.append(SEP);
                sb.append(TYPE_STRING);
                sb.append(SEP);
                sb.append(encodeString(entry.getKey()));
                sb.append(SEP);
                sb.append(TYPE_STRING);
                sb.append(SEP);
                sb.append(encodeString(entry.getValue()));
            }
        } else {
            // protocol version 1.8.0
            sb.append(SEP);
            sb.append(TYPE_VOID);
        }

        return sb.toString();
    }

    public static String writeInit(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_METADATA_INIT);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof MetadataProviderException) {
            sb.append(SUBTYPE_METADATAPROVIDER_EXCEPTION);
        }
        if (exception instanceof VersionException) {
            // VersionException possible here, but used internally and not specified in the protocol
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));

        return sb.toString();
    }

    ///////////////////////////////////////////////////////////////////////////
    // GET ITEM DATA

    public static String[] readGetItemData(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);

        List<String> itemList = new ArrayList<String>();

        while (tokenizer.hasMoreTokens()) {
            String typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    String item = tokenizer.nextToken();
                    itemList.add(decodeString(item));
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_ITEM_DATA + " request");
            }

        }

        String [] items = itemList.toArray(new String [itemList.size()]);
        return items;
    }

    public static String writeGetItemData(ItemData[] itemDatas) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_GET_ITEM_DATA);

        for (ItemData itemData : itemDatas) {
            sb.append(SEP);
            sb.append(TYPE_INT);
            sb.append(SEP);
            sb.append(itemData.distinctSnapshotLength);
            sb.append(SEP);
            sb.append(TYPE_DOUBLE);
            sb.append(SEP);
            sb.append(encodeDouble(itemData.minSourceFrequency));
            sb.append(SEP);
            sb.append(TYPE_MODES);
            sb.append(SEP);
            sb.append(encodeModes(itemData.allowedModes));
        }

        return sb.toString();
    }

    public static String writeGetItemData(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_GET_ITEM_DATA);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // NOTIFY USER

    public static NotifyUserData readNotifyUser(String request, String methodVersion) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);
        String msg = null;
        RemotingException reNU = null;
        
        NotifyUserData data = new NotifyUserData();

        String typ = null;
        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + methodVersion + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.user = decodeString(tokenizer.nextToken());
                break;

            default:
                msg = "Unknown type '" + typ + "' found while parsing a " + methodVersion + " request";
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e2) {
            msg = "Token not found while parsing a " + methodVersion + " request";
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.password = decodeString(tokenizer.nextToken());
                break;

            default:
                msg = "Unknown type '" + typ + "' found while parsing a " + methodVersion + " request";
        }

        if (METHOD_NOTIFY_USER_AUTH.equals(methodVersion)) {
            try {
                typ = tokenizer.nextToken();
            } catch (NoSuchElementException e3) {
                msg = "Token not found while parsing a " + methodVersion + " request";
            }
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    data.clientPrincipal = decodeString(tokenizer.nextToken());
                    break;

                default:
                     msg = "Unknown type '" + typ + "' found while parsing a " + methodVersion + " request";
            }
        }

        while (tokenizer.hasMoreTokens()) {
            String headerName  = "";
            String headerValue = "";

            typ = tokenizer.nextToken();

            String val; // declared here to avoid JVM bug JDK-8067429
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    val = tokenizer.nextToken();
                    headerName = decodeString(val);
                    break;

                default:
                    msg = "Unknown type '" + typ + "' found while parsing a " + methodVersion + " request";
            }

            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    val = tokenizer.nextToken();
                    headerValue = decodeString(val);
                    break;

                default:
                    msg = "Unknown type '" + typ + "' found while parsing a " + methodVersion + " request";
            }
            
            if (msg != null) {
                reNU = new RemotingException(msg);
                
                throw reNU;
            } else {
                data.httpHeaders.put(headerName, headerValue);
            }
        }

        return data;
    }

    public static String writeNotifyUser(UserData userData, String methodVersion) {
        StringBuilder sb = new StringBuilder();

        sb.append(methodVersion);
        sb.append(SEP);
        sb.append(TYPE_DOUBLE);
        sb.append(SEP);
        sb.append(encodeDouble(userData.allowedMaxBandwidth));
        sb.append(SEP);
        sb.append(TYPE_BOOLEAN);
        sb.append(SEP);
        sb.append(userData.wantsTablesNotification ? VALUE_TRUE : VALUE_FALSE);

        return sb.toString();
    }

    public static String writeNotifyUser(Throwable exception, String methodVersion) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(methodVersion);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof AccessException) {
            sb.append(SUBTYPE_ACCESS_EXCEPTION);
        }
        if (exception instanceof CreditsException) {
            sb.append(SUBTYPE_CREDITS_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));
        if (exception instanceof CreditsException) {
            sb.append(SEP);
            sb.append(((CreditsException) exception).getClientErrorCode());
            sb.append(SEP);
            sb.append(encodeString(((CreditsException) exception).getClientErrorMsg()));
        }

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // GET SCHEMA

    public static GetSchemaData readGetSchema(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);

        GetSchemaData data = new GetSchemaData();

        String typ = null;
        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_GET_SCHEMA + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.user = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_SCHEMA + " request");
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e2) {
            throw new RemotingException("Token not found while parsing a " + METHOD_GET_SCHEMA + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.group = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_SCHEMA + " request");
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e3) {
            throw new RemotingException("Token not found while parsing a " + METHOD_GET_SCHEMA + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.schema = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_SCHEMA + " request");
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e4) {
            throw new RemotingException("Token not found while parsing a " + METHOD_GET_SCHEMA + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.session = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_SCHEMA + " request");
        }

        return data;
    }

    public static String writeGetSchema(String[] fields) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_GET_SCHEMA);

        for (String field : fields) {
            sb.append(SEP);
            sb.append(TYPE_STRING);
            sb.append(SEP);
            sb.append(encodeString(field));
        }

        return sb.toString();
    }

    public static String writeGetSchema(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_GET_SCHEMA);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof ItemsException) {
            sb.append(SUBTYPE_ITEMS_EXCEPTION);
        }
        if (exception instanceof SchemaException) {
            sb.append(SUBTYPE_SCHEMA_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // GET ITEMS

    public static GetItemsData readGetItems(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);

        GetItemsData data = new GetItemsData();

        String typ = null;
        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_GET_ITEMS + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.user = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_ITEMS + " request");
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e2) {
            throw new RemotingException("Token not found while parsing a " + METHOD_GET_ITEMS + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.group = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_ITEMS + " request");
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e3) {
            throw new RemotingException("Token not found while parsing a " + METHOD_GET_ITEMS + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.session = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_ITEMS + " request");
        }

        return data;
    }

    public static String writeGetItems(String[] items) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_GET_ITEMS);

        for (String item : items) {
            sb.append(SEP);
            sb.append(TYPE_STRING);
            sb.append(SEP);
            sb.append(encodeString(item));
        }

        return sb.toString();
    }

    public static String writeGetItems(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_GET_ITEMS);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof ItemsException) {
            sb.append(SUBTYPE_ITEMS_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // GET USER ITEM DATA

    public static GetUserItemData readGetUserItemData(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);

        GetUserItemData data = new GetUserItemData();

        String typ = null;
        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_GET_USER_ITEM_DATA + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.user = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_USER_ITEM_DATA + " request");
        }

        List<String> itemList = new ArrayList<String>();

        while (tokenizer.hasMoreTokens()) {
            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    String item = tokenizer.nextToken();
                    itemList.add(decodeString(item));
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_GET_USER_ITEM_DATA + " request");
            }

        }

        String [] items = itemList.toArray(new String [itemList.size()]);
        data.items = items;

        return data;
    }

    public static String writeGetUserItemData(UserItemData[] userItemDatas) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_GET_USER_ITEM_DATA);

        for (UserItemData userItemData : userItemDatas) {
            sb.append(SEP);
            sb.append(TYPE_INT);
            sb.append(SEP);
            sb.append(userItemData.allowedBufferSize);
            sb.append(SEP);
            sb.append(TYPE_DOUBLE);
            sb.append(SEP);
            sb.append(encodeDouble(userItemData.allowedMaxItemFrequency));
            sb.append(SEP);
            sb.append(TYPE_MODES);
            sb.append(SEP);
            sb.append(encodeModes(userItemData.allowedModes));
        }

        return sb.toString();
    }

    public static String writeGetUserItemData(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_GET_USER_ITEM_DATA);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // NOTIFY USER MESSAGE

    public static NotifyUserMessageData readNotifyUserMessage(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);

        NotifyUserMessageData data = new NotifyUserMessageData();

        String typ = null;
        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_USER_MESSAGE + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.user = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_USER_MESSAGE + " request");
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e2) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_USER_MESSAGE + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.session = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_USER_MESSAGE + " request");
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e3) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_USER_MESSAGE + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.message = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_USER_MESSAGE + " request");
        }

        return data;
    }

    public static String writeNotifyUserMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_USER_MESSAGE);
        sb.append(SEP);
        sb.append(TYPE_VOID);

        return sb.toString();
    }

    public static String writeNotifyUserMessage(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_USER_MESSAGE);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof NotificationException) {
            sb.append(SUBTYPE_NOTIFICATION_EXCEPTION);
        }
        if (exception instanceof CreditsException) {
            sb.append(SUBTYPE_CREDITS_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));
        if (exception instanceof CreditsException) {
            sb.append(SEP);
            sb.append(((CreditsException) exception).getClientErrorCode());
            sb.append(SEP);
            sb.append(encodeString(((CreditsException) exception).getClientErrorMsg()));
        }

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // NOTIFY NEW SESSION

    public static NotifyNewSessionData readNotifyNewSession(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);
        String msg = null;
        RemotingException re = null;
        
        NotifyNewSessionData data = new NotifyNewSessionData();

        String typ = null;
        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            msg = "Token not found while parsing a " + METHOD_NOTIFY_NEW_SESSION + " request";
        }
        
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.user = decodeString(tokenizer.nextToken());
                break;

            default:
                msg = "Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_SESSION + " request";
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e2) {
            msg = "Token not found while parsing a " + METHOD_NOTIFY_NEW_SESSION + " request";
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.session = decodeString(tokenizer.nextToken());
                break;

            default:
                msg = "Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_SESSION + " request";
        }

        while (tokenizer.hasMoreTokens()) {
            String contextInfoName = "";
            String contextInfoValue = "";

            typ = tokenizer.nextToken();

            String val; // declared here to avoid JVM bug JDK-8067429
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    val = tokenizer.nextToken();
                    contextInfoName = decodeString(val);
                    break;

                default:
                    msg = "Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_SESSION + " request";
            }

            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    val = tokenizer.nextToken();
                    contextInfoValue = decodeString(val);
                    break;

                default:
                    msg = "Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_SESSION + " request";
            }

            if (msg != null) {
                re = new RemotingException(msg);
                throw re;
            } else {
                data.clientContext.put(contextInfoName, contextInfoValue);
            }
        }

        return data;
    }

    public static String writeNotifyNewSession() {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_NEW_SESSION);
        sb.append(SEP);
        sb.append(TYPE_VOID);

        return sb.toString();
    }

    public static String writeNotifyNewSession(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_NEW_SESSION);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof NotificationException) {
            sb.append(SUBTYPE_NOTIFICATION_EXCEPTION);
        }
        if (exception instanceof CreditsException) {
            if (exception instanceof ConflictingSessionException) {
                sb.append(SUBTYPE_CONFLICTING_SESSION_EXCEPTION);
            } else {
                sb.append(SUBTYPE_CREDITS_EXCEPTION);
            }
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));
        if (exception instanceof CreditsException) {
            sb.append(SEP);
            sb.append(((CreditsException) exception).getClientErrorCode());
            sb.append(SEP);
            sb.append(encodeString(((CreditsException) exception).getClientErrorMsg()));
            if (exception instanceof ConflictingSessionException) {
                sb.append(SEP);
                sb.append(encodeString(((ConflictingSessionException) exception).getConflictingSessionID()));
            }
        }

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // NOTIFY SESSION CLOSE

    public static String readNotifySessionClose(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);

        String typ = null;
        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_SESSION_CLOSE + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                String session = tokenizer.nextToken();
                return decodeString(session);

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_SESSION_CLOSE + " request");
        }
    }

    public static String writeNotifySessionClose() {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_SESSION_CLOSE);
        sb.append(SEP);
        sb.append(TYPE_VOID);

        return sb.toString();
    }

    public static String writeNotifySessionClose(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_SESSION_CLOSE);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof NotificationException) {
            sb.append(SUBTYPE_NOTIFICATION_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // NOTIFY NEW TABLES

    public static NotifyNewTablesData readNotifyNewTables(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);

        NotifyNewTablesData data = new NotifyNewTablesData();

        String typ = null;
        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.user = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
        }

        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e2) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.session = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
        }

        List<TableInfo> tableList = new ArrayList<TableInfo>();

        while (tokenizer.hasMoreTokens()) {
            int winIndex = -1;
            Mode mode = null;
            String id = null;
            String schema = null;
            int min = -1;
            int max = -1;
            String selector = null;

            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_INT:
                    winIndex = Integer.parseInt(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }

            try {
                typ = tokenizer.nextToken();
            } catch (NoSuchElementException e3) {
                throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }
            switch (typ.toCharArray()[0]) {

                case TYPE_MODES:
                    Mode [] modes = decodeModes(tokenizer.nextToken());
                    if (modes != null) {
                        mode = modes[0];
                    }
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }

            try {
                typ = tokenizer.nextToken();
            } catch (NoSuchElementException e4) {
                throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    id = decodeString(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }

            try {
                typ = tokenizer.nextToken();
            } catch (NoSuchElementException e5) {
                throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    schema = decodeString(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }

            if (!tokenizer.hasMoreTokens()) {
                break;
            }
            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_INT:
                    min = Integer.parseInt(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }

            if (!tokenizer.hasMoreTokens()) {
                break;
            }
            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_INT:
                    max = Integer.parseInt(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }

            try {
                typ = tokenizer.nextToken();
            } catch (NoSuchElementException e6) {
                throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    selector = decodeString(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_NEW_TABLES + " request");
            }

            TableInfo table = new TableInfo(winIndex, mode, id, schema, min, max, selector);
            tableList.add(table);

        }
  
        TableInfo [] tables = tableList.toArray(new TableInfo [tableList.size()]);
        data.tables = tables;

        return data;
    }

    public static String writeNotifyNewTables() {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_NEW_TABLES);
        sb.append(SEP);
        sb.append(TYPE_VOID);

        return sb.toString();
    }

    public static String writeNotifyNewTables(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_NEW_TABLES);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof NotificationException) {
            sb.append(SUBTYPE_NOTIFICATION_EXCEPTION);
        }
        if (exception instanceof CreditsException) {
            sb.append(SUBTYPE_CREDITS_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));
        if (exception instanceof CreditsException) {
            sb.append(SEP);
            sb.append(((CreditsException) exception).getClientErrorCode());
            sb.append(SEP);
            sb.append(encodeString(((CreditsException) exception).getClientErrorMsg()));
        }

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // NOTIFY TABLES CLOSE

    public static NotifyTablesCloseData readNotifyTablesClose(String request) throws RemotingException {
        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);

        NotifyTablesCloseData data = new NotifyTablesCloseData();

        String typ = null;
        try {
            typ = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
        }
        switch (typ.toCharArray()[0]) {

            case TYPE_STRING:
                data.session = decodeString(tokenizer.nextToken());
                break;

            default:
                throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
        }

        List<TableInfo> tableList = new ArrayList<TableInfo>();

        while (tokenizer.hasMoreTokens()) {
            int winIndex = -1;
            Mode mode = null;
            String id = null;
            String schema = null;
            int min = -1;
            int max = -1;
            String selector = null;

            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_INT:
                    winIndex = Integer.parseInt(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }

            try {
                typ = tokenizer.nextToken();
            } catch (NoSuchElementException e2) {
                throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }
            switch (typ.toCharArray()[0]) {

                case TYPE_MODES:
                    Mode [] modes = decodeModes(tokenizer.nextToken());
                    if (modes != null) {
                        mode = modes[0];
                    }
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }

            try {
                typ = tokenizer.nextToken();
            } catch (NoSuchElementException e3) {
                throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    id = decodeString(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }

            try {
                typ = tokenizer.nextToken();
            } catch (NoSuchElementException e4) {
                throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    schema = decodeString(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }

            if (!tokenizer.hasMoreTokens()) {
                break;
            }
            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_INT:
                    min = Integer.parseInt(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }

            if (!tokenizer.hasMoreTokens()) {
                break;
            }
            typ = tokenizer.nextToken();

            switch (typ.toCharArray()[0]) {

                case TYPE_INT:
                    max = Integer.parseInt(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }

            try {
                typ = tokenizer.nextToken();
            } catch (NoSuchElementException e5) {
                throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }
            switch (typ.toCharArray()[0]) {

                case TYPE_STRING:
                    selector = decodeString(tokenizer.nextToken());
                    break;

                default:
                    throw new RemotingException("Unknown type '" + typ + "' found while parsing a " + METHOD_NOTIFY_TABLES_CLOSE + " request");
            }

            TableInfo table = new TableInfo(winIndex, mode, id, schema, min, max, selector);
            tableList.add(table);

        }

        TableInfo [] tables = tableList.toArray(new TableInfo [tableList.size()]);
        data.tables = tables;

        return data;
    }

    public static String writeNotifyTablesClose() {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_TABLES_CLOSE);
        sb.append(SEP);
        sb.append(TYPE_VOID);

        return sb.toString();
    }

    public static String writeNotifyTablesClose(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_TABLES_CLOSE);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof NotificationException) {
            sb.append(SUBTYPE_NOTIFICATION_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // NOTIFY MPN DEVICE ACCESS

    public static NotifyMpnDeviceAccessData readNotifyMpnDeviceAccess(String request) throws RemotingException {
        NotifyMpnDeviceAccessData data = new NotifyMpnDeviceAccessData();

        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);
        try {

            // User
            String typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_ACCESS + " request");
            }

            data.user = decodeString(tokenizer.nextToken());

            // Session ID
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_ACCESS + " request");
            }

            data.sessionID = decodeString(tokenizer.nextToken());

            // Platform type
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_MPN_PLATFORM) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_ACCESS + " request");
            }

            MpnPlatformType platformType = decodeMpnPlatformType(tokenizer.nextToken());

            // Application ID
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_ACCESS + " request");
            }

            String appID = decodeString(tokenizer.nextToken());

            // Device token
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_ACCESS + " request");
            }

            String deviceToken = decodeString(tokenizer.nextToken());

            data.device = new MpnDeviceInfo(platformType, appID, deviceToken);

        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_ACCESS + " request");
        }

        return data;
    }

    public static String writeNotifyMpnDeviceAccess() {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_MPN_DEVICE_ACCESS);
        sb.append(SEP);
        sb.append(TYPE_VOID);

        return sb.toString();
    }

    public static String writeNotifyMpnDeviceAccess(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_MPN_DEVICE_ACCESS);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof NotificationException) {
            sb.append(SUBTYPE_NOTIFICATION_EXCEPTION);
        }
        if (exception instanceof CreditsException) {
            sb.append(SUBTYPE_CREDITS_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));
        if (exception instanceof CreditsException) {
            sb.append(SEP);
            sb.append(((CreditsException) exception).getClientErrorCode());
            sb.append(SEP);
            sb.append(encodeString(((CreditsException) exception).getClientErrorMsg()));
        }

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // NOTIFY MPN SUBSCRIPTION ACTIVATION

    public static NotifyMpnSubscriptionActivationData readNotifyMpnSubscriptionActivation(String request) throws RemotingException {
        NotifyMpnSubscriptionActivationData data = new NotifyMpnSubscriptionActivationData();

        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);
        try {

            // User
            String typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            data.user = decodeString(tokenizer.nextToken());

            // Session ID
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            data.sessionID = decodeString(tokenizer.nextToken());

            // Table info: win index
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_INT) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            int winIndex = Integer.parseInt(tokenizer.nextToken());

            // Table info: mode
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_MODES) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            Mode [] modes = decodeModes(tokenizer.nextToken());
            Mode mode = (modes != null) ? modes [0] : null;

            // Table info: ID
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            String id = decodeString(tokenizer.nextToken());

            // Table info: schema
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            String schema = decodeString(tokenizer.nextToken());

            // Table info: min
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_INT) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            int min = Integer.parseInt(tokenizer.nextToken());

            // Table info: max
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_INT) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            int max = Integer.parseInt(tokenizer.nextToken());

            TableInfo table = new TableInfo(winIndex, mode, id, schema, min, max, null);
            data.table = table;

            // Platform type
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_MPN_PLATFORM) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            MpnPlatformType platformType = decodeMpnPlatformType(tokenizer.nextToken());

            // Application ID
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            String appID = decodeString(tokenizer.nextToken());

            // Device token
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            String deviceToken = decodeString(tokenizer.nextToken());

            // Trigger
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            String trigger = decodeString(tokenizer.nextToken());

            // Notification format
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
            }

            String notificationFormat = decodeString(tokenizer.nextToken());

            MpnDeviceInfo deviceInfo = new MpnDeviceInfo(platformType, appID, deviceToken);
            MpnSubscriptionInfo subscription = new MpnSubscriptionInfo(deviceInfo, notificationFormat, trigger);

            data.mpnSubscription = subscription;

        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION + " request");
        }

        return data;
    }

    public static String writeNotifyMpnSubscriptionActivation() {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION);
        sb.append(SEP);
        sb.append(TYPE_VOID);

        return sb.toString();
    }

    public static String writeNotifyMpnSubscriptionActivation(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof NotificationException) {
            sb.append(SUBTYPE_NOTIFICATION_EXCEPTION);
        }
        if (exception instanceof CreditsException) {
            sb.append(SUBTYPE_CREDITS_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));
        if (exception instanceof CreditsException) {
            sb.append(SEP);
            sb.append(((CreditsException) exception).getClientErrorCode());
            sb.append(SEP);
            sb.append(encodeString(((CreditsException) exception).getClientErrorMsg()));
        }

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // NOTIFY MPN DEVICE TOKEN CHANGE

    public static NotifyMpnDeviceTokenChangeData readNotifyMpnDeviceTokenChange(String request) throws RemotingException {
        NotifyMpnDeviceTokenChangeData data = new NotifyMpnDeviceTokenChangeData();

        StringTokenizer tokenizer = new StringTokenizer(request, "" + SEP);
        try {

            // User
            String typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE + " request");
            }

            data.user = decodeString(tokenizer.nextToken());

            // Session ID
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE + " request");
            }

            data.sessionID = decodeString(tokenizer.nextToken());

            // Platform type
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_MPN_PLATFORM) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE + " request");
            }

            MpnPlatformType platformType = decodeMpnPlatformType(tokenizer.nextToken());

            // Application ID
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE + " request");
            }

            String appID = decodeString(tokenizer.nextToken());

            // Device token
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE + " request");
            }

            String deviceToken = decodeString(tokenizer.nextToken());

            data.device = new MpnDeviceInfo(platformType, appID, deviceToken);

            // New device token
            typ = tokenizer.nextToken();
            if (typ.toCharArray()[0] != TYPE_STRING) {
                throw new RemotingException("Unexpected type '" + typ + "' found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE + " request");
            }

            String newDeviceToken = decodeString(tokenizer.nextToken());

            data.newDeviceToken = newDeviceToken;

        } catch (NoSuchElementException e) {
            throw new RemotingException("Token not found while parsing a " + METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE + " request");
        }

        return data;
    }

    public static String writeNotifyMpnDeviceTokenChange() {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE);
        sb.append(SEP);
        sb.append(TYPE_VOID);

        return sb.toString();
    }

    public static String writeNotifyMpnDeviceTokenChange(Throwable exception) throws RemotingException {
        StringBuilder sb = new StringBuilder();

        sb.append(METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE);
        sb.append(SEP);
        sb.append(TYPE_EXCEPTION);
        if (exception instanceof NotificationException) {
            sb.append(SUBTYPE_NOTIFICATION_EXCEPTION);
        }
        if (exception instanceof CreditsException) {
            sb.append(SUBTYPE_CREDITS_EXCEPTION);
        }
        sb.append(SEP);
        sb.append(encodeString(exception.getMessage()));
        if (exception instanceof CreditsException) {
            sb.append(SEP);
            sb.append(((CreditsException) exception).getClientErrorCode());
            sb.append(SEP);
            sb.append(encodeString(((CreditsException) exception).getClientErrorMsg()));
        }

        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // Internal methods

    protected static String encodeModes(Mode[] modes) throws RemotingException {
        if (modes == null) {
            return VALUE_NULL;
        }
        if (modes.length == 0) {
            return VALUE_EMPTY;
        }

        StringBuilder encodedModes = new StringBuilder();

        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(Mode.RAW)) {
                encodedModes.append(TYPE_MODE_RAW);
            } else if (modes[i].equals(Mode.MERGE)) {
                encodedModes.append(TYPE_MODE_MERGE);
            } else if (modes[i].equals(Mode.DISTINCT)) {
                encodedModes.append(TYPE_MODE_DISTINCT);
            } else if (modes[i].equals(Mode.COMMAND)) {
                encodedModes.append(TYPE_MODE_COMMAND);
            } else {
                throw new RemotingException("Unknown mode '" + modes[i].toString() + "' found while encoding Mode array");
            }
        }

        return encodedModes.toString();
    }

    protected static Mode[] decodeModes(String str) throws RemotingException {
        if (str.equals(VALUE_NULL)) {
            return null;
        }
        if (str.equals(VALUE_EMPTY)) {
            return new Mode [0];
        }

        Mode [] modes = new Mode [str.length()];

        char [] encodedModes = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {
            char encodedMode = encodedModes[i];
            switch (encodedMode) {
                case TYPE_MODE_RAW:
                    modes[i] = Mode.RAW;
                    break;
                case TYPE_MODE_MERGE:
                    modes[i] = Mode.MERGE;
                    break;
                case TYPE_MODE_DISTINCT:
                    modes[i] = Mode.DISTINCT;
                    break;
                case TYPE_MODE_COMMAND:
                    modes[i] = Mode.COMMAND;
                    break;
                default:
                    throw new RemotingException("Unknown mode '" + encodedMode + "' found while decoding Mode array");
            }
        }

        return modes;
    }

    protected static String encodeDouble(double val) {
        return Double.toString(val);
    }

    protected static double decodeDouble(String str) {
        return Double.parseDouble(str);
    }

    protected static char encodeMpnPlatformType(MpnPlatformType platformType) throws RemotingException {
        if (platformType == null) {
            return VALUE_NULL.toCharArray()[0];
        }

        if (platformType.equals(MpnPlatformType.Apple.toString())) {
            return TYPE_MPN_PLATFORM_APPLE;
        } else if (platformType.equals(MpnPlatformType.Google.toString())) {
            return TYPE_MPN_PLATFORM_GOOGLE;
        } else {
            throw new RemotingException("Unknown platform type '" + platformType.toString() + "'");
        }
    }

    protected static MpnPlatformType decodeMpnPlatformType(String str) throws RemotingException {
        if (str.equals(VALUE_NULL)) {
            return null;
        }

        char encodedPlatformType = str.toCharArray()[0];
        switch (encodedPlatformType) {
            case TYPE_MPN_PLATFORM_APPLE:
                return MpnPlatformType.Apple;
            case TYPE_MPN_PLATFORM_GOOGLE:
                return MpnPlatformType.Google;
            default:
                throw new RemotingException("Unknown platform type '" + encodedPlatformType + "'");
        }
    }
}