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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.lightstreamer.log.LogManager;
import com.lightstreamer.log.Logger;

class MetadataProviderServerImpl extends ServerImpl {
    private static Logger _log = LogManager.getLogger("com.lightstreamer.adapters.remote.Server.MetadataProviderServer");

    private boolean _initExpected;
    private boolean _closeExpected;
    private boolean _initializeOnStart;
    private MetadataProvider _adapter;
    private Map<String,String> _adapterParams;
    private String _adapterConfig;

    private final String _poolType;
    private final ExecutorService _executor;
            
    public MetadataProviderServerImpl(boolean initializeOnStart) {
        _initializeOnStart = initializeOnStart;
            // set to true to force the old behavior (for an old Proxy Adapter)
        _initExpected = true;
        _closeExpected = true;
            // we start with the current version of the protocol, which does not conflict with earlier versions
        _adapter = null;
        _adapterParams = new HashMap<String,String>();
        _adapterConfig = null;
        
        String threadsConf = System.getProperty("lightstreamer.metadata.pool.size");
        if (threadsConf == null) {
            threadsConf = "0";
        }
        try {
            int threads = Integer.parseInt(threadsConf);
            if (threads <= 0) {
                _poolType = "unlimited thread pool";
                _executor = Executors.newCachedThreadPool();
            } else if (threads == 1) {
                _poolType = "sequential invocations";
                _executor = Executors.newSingleThreadExecutor();
            } else {
                _poolType = "thread pool size = " + threads;
                _executor = Executors.newFixedThreadPool(threads);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid lightstreamer.metadata.pool.size configuration: " + threadsConf);
        }
    }

    public final void setAdapter(MetadataProvider value) {
        _adapter = value;
    }
    public final MetadataProvider getAdapter() {
        return _adapter;
    }

    public final void setAdapterParams(Map<String,String> value) {
        _adapterParams = value;
    }
    public final Map<String,String> getAdapterParams() {
        return _adapterParams;
    }

    public final void setAdapterConfig(String value) {
        _adapterConfig = value;
    }
    public final String getAdapterConfig() {
        return _adapterConfig;
    }

    @Override
    public void start() throws MetadataProviderException, DataProviderException, RemotingException {
        _log.info("Managing Metadata Adapter " + super.getName() + " with " + _poolType);
        if (_initializeOnStart) {
            // requires to start already initialized (old behavior)
            _adapter.init(_adapterParams, _adapterConfig);
        }

        super.start();

        Map<String, String> credentials = getCredentialParams(_closeExpected);
        if (credentials != null) {
            sendRemoteCredentials(credentials);
        }
    }

    public void sendRemoteCredentials(Map<String,String> credentials) throws RemotingException {
        String notify = MetadataProviderProtocol.writeRemoteCredentials(credentials);
        RequestReceiver currRequestReceiver;
        synchronized (this) {
            currRequestReceiver = _requestReceiver;
        }
        if (currRequestReceiver != null) {
            currRequestReceiver.sendUnsolicitedMessage(MetadataProviderProtocol.AUTH_REQUEST_ID, notify, _log);
        }
    }

    @Override
    public void onRequestReceived(String requestId, String request) {
        int sep = request.indexOf(RemotingProtocol.SEP);
        if (sep < 1) {
            _log.warn("Discarding malformed request: " + request);
            return;
        }

        String method = request.substring(0, sep);

        try {
            if (method.equals(MetadataProviderProtocol.METHOD_CLOSE) && _closeExpected) {
                // this can also precede the init request
                if (! requestId.equals(MetadataProviderProtocol.CLOSE_REQUEST_ID)) {
                    throw new RemotingException("Unexpected id found while parsing a " + MetadataProviderProtocol.METHOD_CLOSE + " request");
                }
                final Map<String, String> closeParams = MetadataProviderProtocol.readClose(request.substring(sep + 1));
                String closeReason = closeParams.get(MetadataProviderProtocol.KEY_CLOSE_REASON);
                if (closeReason != null) {
                    throw new RemotingException("Close requested by the counterpart with reason: " + closeReason);
                } else {
                    throw new RemotingException("Close requested by the counterpart");
                }
            }

            boolean isInitRequest = method.equals(MetadataProviderProtocol.METHOD_METADATA_INIT);
            if (isInitRequest && !_initExpected) {
                throw new RemotingException("Unexpected late " + MetadataProviderProtocol.METHOD_METADATA_INIT + " request");
            } else if (!isInitRequest && _initExpected) {
                if (!_initializeOnStart) {
                    throw new RemotingException("Unexpected request " + request + " while waiting for a " + MetadataProviderProtocol.METHOD_METADATA_INIT + " request");
                } else {
                    _initExpected = false; // init request not received, but now no longer possible
                }
            }

            if (isInitRequest) {
                _log.debug("Processing request: " + requestId);
                String reply;
                String keepaliveHint = null;
                _initExpected = false;
                // NOTE: compacting the two branches below is more complicated than it seems
                if (!_initializeOnStart) {
                    Map<String,String> initParams = MetadataProviderProtocol.readInit(request.substring(sep + 1));
                    try {
                        String proxyVersion = initParams.get(PROTOCOL_VERSION_PARAM);
                        String advertisedVersion = getSupportedVersion(proxyVersion);
                            // this may prevent the initialization
                        boolean is180 = (advertisedVersion == null);
                        boolean is182 = (advertisedVersion != null && advertisedVersion.equals("1.8.2"));
                        
                        if (is180 || is182) {
                            if (_closeExpected) {
                                // WARNING: these versions don't provide for the CLOSE message,
                                // but we previously asked for the CLOSE message with the RAC message;
                                // hence we should no longer expect a CLOSE message, but only after
                                // the client receives this answer, which confirms the protocol;
                                // however, assuming that the Proxy Adapter only supports these versions,
                                // we expect that it has ignored our request in the RAC message,
                                // hence we can already stop expecting a CLOSE message.
                            }
                            _closeExpected = false; 
                        }
                        if (! is180) {
                            // protocol version 1.8.2 and above
                            keepaliveHint = initParams.get(KEEPALIVE_HINT_PARAM);
                            if (keepaliveHint == null) {
                                keepaliveHint = "0";
                            }
                            initParams.remove(PROTOCOL_VERSION_PARAM);
                            initParams.remove(KEEPALIVE_HINT_PARAM);
                            // the version and keepalive hint are internal parameters, not to be sent to the custom Adapter
                        }

                        Iterator<String> paramIter = _adapterParams.keySet().iterator();
                        while (paramIter.hasNext()) {
                            String param = paramIter.next();
                            initParams.put(param, _adapterParams.get(param));
                        }
                        _adapter.init(initParams, _adapterConfig);

                        if (! is180) {
                            // protocol version 1.8.2 and above
                            Map<String,String> _proxyParams = new HashMap<>();
                            _proxyParams.put(PROTOCOL_VERSION_PARAM, advertisedVersion);
                            reply = MetadataProviderProtocol.writeInit(_proxyParams);
                        } else {
                            // protocol version 1.8.0
                            reply = MetadataProviderProtocol.writeInit((Map<String, String>) null);
                        }
                    } catch (MetadataProviderException | VersionException | Error | RuntimeException e) {
                        reply = MetadataProviderProtocol.writeInit(e);
                    }

                } else {
                    _log.warn("Received Metadata Adapter initialization request; parameters ignored");
                    Map<String,String> initParams = MetadataProviderProtocol.readInit(request.substring(sep + 1));
                    try {
                        String proxyVersion = initParams.get(PROTOCOL_VERSION_PARAM);
                        String advertisedVersion = getSupportedVersion(proxyVersion);
                        boolean is180 = (advertisedVersion == null);
                        boolean is182 = (advertisedVersion != null && advertisedVersion.equals("1.8.2"));

                        if (is180 || is182) {
                            if (_closeExpected) {
                                // WARNING: these versions don't provide for the CLOSE message,
                                // but we previously asked for the CLOSE message with the RAC message;
                                // hence we should no longer expect a CLOSE message, but only after
                                // the client receives this answer, which confirms the protocol;
                                // however, assuming that the Proxy Adapter only supports these versions,
                                // we expect that it has ignored our request in the RAC message,
                                // hence we can already stop expecting a CLOSE message.
                            }
                            _closeExpected = false;
                        }
                        if (! is180) {
                            // protocol version 1.8.2 and above
                            keepaliveHint = initParams.get(KEEPALIVE_HINT_PARAM);
                            if (keepaliveHint == null) {
                                keepaliveHint = "0";
                            }
                            Map<String,String> _proxyParams = new HashMap<>();
                            _proxyParams.put(PROTOCOL_VERSION_PARAM, advertisedVersion);
                            reply = MetadataProviderProtocol.writeInit(_proxyParams);
                        } else {
                            // protocol version 1.8.0
                            reply = MetadataProviderProtocol.writeInit((Map<String, String>) null);
                        }
                    } catch (VersionException | Error | RuntimeException e) {
                        reply = MetadataProviderProtocol.writeInit(e);
                        // here the Remote Adapter is already initialized
                        // and we should notify custom code of the issue;
                        // but now the Proxy Adapter will terminate the connection and we lean on that
                    }
                }

                useKeepaliveHint(keepaliveHint);
                sendReply(requestId, reply);

            } else if (method.equals(MetadataProviderProtocol.METHOD_GET_ITEM_DATA)) {
                final String [] items = MetadataProviderProtocol.readGetItemData(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            ItemData [] itemDatas = new ItemData [items.length];
                            for (int i = 0; i < items.length; i++) {
                                List<Mode> modeList = new ArrayList<Mode>(4);
                                if (_adapter.modeMayBeAllowed(items[i], Mode.RAW)) {
                                    modeList.add(Mode.RAW);
                                }
                                if (_adapter.modeMayBeAllowed(items[i], Mode.MERGE)) {
                                    modeList.add(Mode.MERGE);
                                }
                                if (_adapter.modeMayBeAllowed(items[i], Mode.DISTINCT)) {
                                    modeList.add(Mode.DISTINCT);
                                }
                                if (_adapter.modeMayBeAllowed(items[i], Mode.COMMAND)) {
                                    modeList.add(Mode.COMMAND);
                                }
                                
                                Mode [] modes = modeList.toArray(new Mode [modeList.size()]);
                         
                                itemDatas[i] = new ItemData();
                                itemDatas[i].allowedModes = modes;
                                itemDatas[i].distinctSnapshotLength = _adapter.getDistinctSnapshotLength(items[i]);
                                itemDatas[i].minSourceFrequency = _adapter.getMinSourceFrequency(items[i]);
                            }
                            return MetadataProviderProtocol.writeGetItemData(itemDatas);
                        } catch (Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeGetItemData(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_USER)) {
                final NotifyUserData notifyUserData = MetadataProviderProtocol.readNotifyUser(request.substring(sep + 1), MetadataProviderProtocol.METHOD_NOTIFY_USER);
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifyUser(notifyUserData.user, notifyUserData.password, notifyUserData.httpHeaders);
                            UserData userData = new UserData();
                            userData.allowedMaxBandwidth = _adapter.getAllowedMaxBandwidth(notifyUserData.user);
                            userData.wantsTablesNotification = _adapter.wantsTablesNotification(notifyUserData.user);
                            return MetadataProviderProtocol.writeNotifyUser(userData, MetadataProviderProtocol.METHOD_NOTIFY_USER);
                        } catch (AccessException | CreditsException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifyUser(e, MetadataProviderProtocol.METHOD_NOTIFY_USER);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_USER_AUTH)) {
                final NotifyUserData notifyUserData = MetadataProviderProtocol.readNotifyUser(request.substring(sep + 1), MetadataProviderProtocol.METHOD_NOTIFY_USER_AUTH);
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifyUser(notifyUserData.user, notifyUserData.password, notifyUserData.httpHeaders, notifyUserData.clientPrincipal);
                            UserData userData = new UserData();
                            userData.allowedMaxBandwidth = _adapter.getAllowedMaxBandwidth(notifyUserData.user);
                            userData.wantsTablesNotification = _adapter.wantsTablesNotification(notifyUserData.user);
                            return MetadataProviderProtocol.writeNotifyUser(userData, MetadataProviderProtocol.METHOD_NOTIFY_USER_AUTH);
                        } catch (AccessException | CreditsException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifyUser(e, MetadataProviderProtocol.METHOD_NOTIFY_USER_AUTH);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_GET_SCHEMA)) {
                final GetSchemaData getSchemaData = MetadataProviderProtocol.readGetSchema(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            String [] fields = _adapter.getSchema(getSchemaData.user, getSchemaData.session, getSchemaData.group, getSchemaData.schema);
                            if (fields == null) {
                                fields = new String [0];
                            }
                            if (fields.length == 0) {
                                _log.warn("Null or empty field list from getSchema for schema '" + getSchemaData.schema + "' in group '" + getSchemaData.group + "'");
                            }
                            return MetadataProviderProtocol.writeGetSchema(fields);
                        } catch (ItemsException | SchemaException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeGetSchema(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_GET_ITEMS)) {
                final GetItemsData getItemsData = MetadataProviderProtocol.readGetItems(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            String [] items = _adapter.getItems(getItemsData.user, getItemsData.session, getItemsData.group);
                            if (items == null) {
                                items = new String [0];
                            }
                            if (items.length == 0) {
                                _log.warn("Null or empty item list from getItems for group '" + getItemsData.group + "'");
                            }
                            return MetadataProviderProtocol.writeGetItems(items);
                        } catch (ItemsException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeGetItems(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_GET_USER_ITEM_DATA)) {
                final GetUserItemData getUserItemData = MetadataProviderProtocol.readGetUserItemData(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            UserItemData [] userItemDatas = new UserItemData[getUserItemData.items.length];
                            for (int i = 0; i < getUserItemData.items.length; i++) {
                                List<Mode> modeList = new ArrayList<Mode>(4);
                                if (_adapter.isModeAllowed(getUserItemData.user, getUserItemData.items[i], Mode.RAW)) {
                                    modeList.add(Mode.RAW);
                                }
                                if (_adapter.isModeAllowed(getUserItemData.user, getUserItemData.items[i], Mode.MERGE)) {
                                    modeList.add(Mode.MERGE);
                                }
                                if (_adapter.isModeAllowed(getUserItemData.user, getUserItemData.items[i], Mode.DISTINCT)) {
                                    modeList.add(Mode.DISTINCT);
                                }
                                if (_adapter.isModeAllowed(getUserItemData.user, getUserItemData.items[i], Mode.COMMAND)) {
                                    modeList.add(Mode.COMMAND);
                                }
                                
                                Mode [] modes = modeList.toArray(new Mode [modeList.size()]);
                             
                                userItemDatas[i] = new UserItemData();
                                userItemDatas[i].allowedModes = modes;
                                userItemDatas[i].allowedMaxItemFrequency = _adapter.getAllowedMaxItemFrequency(getUserItemData.user, getUserItemData.items[i]);
                                userItemDatas[i].allowedBufferSize = _adapter.getAllowedBufferSize(getUserItemData.user, getUserItemData.items[i]);
                            }
                            return MetadataProviderProtocol.writeGetUserItemData(userItemDatas);
                        } catch (Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeGetUserItemData(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_USER_MESSAGE)) {
                final NotifyUserMessageData notifyUserMessageData = MetadataProviderProtocol.readNotifyUserMessage(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifyUserMessage(notifyUserMessageData.user, notifyUserMessageData.session, notifyUserMessageData.message);
                            return MetadataProviderProtocol.writeNotifyUserMessage();
                        } catch (CreditsException | NotificationException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifyUserMessage(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_NEW_SESSION)) {
                final NotifyNewSessionData notifyNewSessionData = MetadataProviderProtocol.readNotifyNewSession(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifyNewSession(notifyNewSessionData.user, notifyNewSessionData.session, notifyNewSessionData.clientContext);
                            return MetadataProviderProtocol.writeNotifyNewSession();
                        } catch (CreditsException | NotificationException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifyNewSession(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_SESSION_CLOSE)) {
                final String session = MetadataProviderProtocol.readNotifySessionClose(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifySessionClose(session);
                            return MetadataProviderProtocol.writeNotifySessionClose();
                        } catch (NotificationException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifySessionClose(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_NEW_TABLES)) {
                final NotifyNewTablesData notifyNewTablesData = MetadataProviderProtocol.readNotifyNewTables(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifyNewTables(notifyNewTablesData.user, notifyNewTablesData.session, notifyNewTablesData.tables);
                            return MetadataProviderProtocol.writeNotifyNewTables();
                        } catch (NotificationException | CreditsException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifyNewTables(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_TABLES_CLOSE)) {
                final NotifyTablesCloseData notifyTablesCloseData = MetadataProviderProtocol.readNotifyTablesClose(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifyTablesClose(notifyTablesCloseData.session, notifyTablesCloseData.tables);
                            return MetadataProviderProtocol.writeNotifyTablesClose();
                        } catch (NotificationException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifyTablesClose(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_MPN_DEVICE_ACCESS)) {
                final NotifyMpnDeviceAccessData notifyMpnDeviceAccessData = MetadataProviderProtocol.readNotifyMpnDeviceAccess(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifyMpnDeviceAccess(notifyMpnDeviceAccessData.user, notifyMpnDeviceAccessData.sessionID, notifyMpnDeviceAccessData.device);
                            return MetadataProviderProtocol.writeNotifyMpnDeviceAccess();
                        } catch (CreditsException | NotificationException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifyMpnDeviceAccess(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_MPN_SUBSCRIPTION_ACTIVATION)) {
                final NotifyMpnSubscriptionActivationData notifyMpnSubscriptionActivationData = MetadataProviderProtocol.readNotifyMpnSubscriptionActivation(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifyMpnSubscriptionActivation(notifyMpnSubscriptionActivationData.user, notifyMpnSubscriptionActivationData.sessionID, notifyMpnSubscriptionActivationData.table, notifyMpnSubscriptionActivationData.mpnSubscription);
                            return MetadataProviderProtocol.writeNotifyMpnSubscriptionActivation();
                        } catch (CreditsException | NotificationException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifyMpnSubscriptionActivation(e);
                        }
                    }
                });

            } else if (method.equals(MetadataProviderProtocol.METHOD_NOTIFY_MPN_DEVICE_TOKEN_CHANGE)) {
                final NotifyMpnDeviceTokenChangeData notifyMpnDeviceTokenChangeData = MetadataProviderProtocol.readNotifyMpnDeviceTokenChange(request.substring(sep + 1));
                executeAndReply(requestId, new Worker() {
                    public String doWork() throws RemotingException {
                        try {
                            _adapter.notifyMpnDeviceTokenChange(notifyMpnDeviceTokenChangeData.user, notifyMpnDeviceTokenChangeData.sessionID, notifyMpnDeviceTokenChangeData.device, notifyMpnDeviceTokenChangeData.newDeviceToken);
                            return MetadataProviderProtocol.writeNotifyMpnDeviceTokenChange();
                        } catch (CreditsException | NotificationException | Error | RuntimeException e) {
                            return MetadataProviderProtocol.writeNotifyMpnDeviceTokenChange(e);
                        }
                    }
                });

            } else {
                _log.warn("Discarding unknown request: " + request);
            }

        } catch (RemotingException e) {
            onException(e);
        }
    }
    
    private interface Worker {
        String doWork() throws RemotingException;
    }
    
    private void executeAndReply(final String requestId, final Worker task) {
        _executor.submit(new Runnable() {
            public void run() {
                try {
                    _log.debug("Processing request: " + requestId);
                    String reply = task.doWork();
                    sendReply(requestId, reply);
                } catch (RemotingException e) {
                    onException(e);
                }
            }
        });
    }
    
    private void sendReply(String requestId, String reply) {
        RequestReceiver currRequestReceiver;
        synchronized (this) {
            currRequestReceiver = _requestReceiver;
        }
        if (currRequestReceiver != null) {
            currRequestReceiver.sendReply(requestId, reply, _log);
        }
    }
    
    @Override
    protected void onDispose() {
        _executor.shutdown();
    }

    @Override
    public boolean handleIOException(IOException exception) {
        _log.error("Caught exception: " + exception.getMessage(), exception);
        Throwable cause = exception.getCause();
        if (cause != null) {
            _log.fatal("Exception caught while reading/writing from/to streams: '" + cause.getMessage() + "', aborting...", cause);
        } else {
            _log.fatal("Exception caught while reading/writing from/to streams: '" + exception.getMessage() + "', aborting...", exception);
        }

        System.exit(0);
        return false;
    }

    @Override
    public boolean handleException(RemotingException exception) {
        _log.error("Caught exception: " + exception.getMessage(), exception);
        return false;
    }
}