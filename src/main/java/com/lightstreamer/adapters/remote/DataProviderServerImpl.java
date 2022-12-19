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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.lightstreamer.log.LogManager;
import com.lightstreamer.log.Logger;

class DataProviderServerImpl extends ServerImpl implements ItemEventListener {
    private static Logger _log = LogManager.getLogger("com.lightstreamer.adapters.remote.Server.DataProviderServer");

    private boolean _initExpected;
    private DataProvider _adapter;
    private Map<String,String> _adapterParams;
    private String _adapterConfig;
    private SubscriptionHelper _helper;

    public DataProviderServerImpl() {
        _initExpected = true;
        _adapter = null;
        _adapterParams = new HashMap<String,String>();
        _adapterConfig = null;
        _helper = new SubscriptionHelper();
    }

    public final void setAdapter(DataProvider value) {
        _adapter = value;
    }
    public final DataProvider getAdapter() {
        return _adapter;
    }

    public final void setAdapterParams(Map<String,String>  value) {
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
    public void start() throws RemotingException {
        _log.info("Managing Data Adapter " + super.getName() + " with " + _helper.getPoolType());

        init();
        synchronized (this) {
            if (_notifySender == null) {
                throw new RemotingException("Notification channel not established: can't start (please check that a valid notification TCP port has been specified)");
            }
        }
        startOut();

        Map<String, String> credentials = getCredentialParams(true);
        if (credentials != null) {
            sendRemoteCredentials(credentials);
        }
        startIn();
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
    
    private void sendNotify(String notify) {
        NotifySender currNotifySender;
        synchronized (this) {
            currNotifySender = _notifySender;
        }
        if (currNotifySender != null) {
            currNotifySender.sendNotify(notify);
        }
    }
    
    @Override
    protected void onDispose() {
        // Notify the SubscriptionHelper that this server object is disposed.
        _helper.shutdown();
    }

    private boolean executeSubscribe(SubscribeData data, String requestId) throws RemotingException {
        _log.debug("Processing request: " + requestId);
        String reply = null;
        boolean success = false;
        try {
            boolean snapshotAvailable = _adapter.isSnapshotAvailable(data.itemName);
            if (!snapshotAvailable) {
                endOfSnapshot(data.itemName);
            }
            _adapter.subscribe(data.itemName);
            reply = DataProviderProtocol.writeSubscribe();
            success = true;
        } catch (SubscriptionException | FailureException  | Error | RuntimeException e) {
            reply = DataProviderProtocol.writeSubscribe(e);
        }

        sendReply(requestId, reply);
        return success;
    }

    private void refuseLateSubscribe(SubscribeData data, String requestId) throws RemotingException {
        _log.debug("Skipping request: " + requestId);
        SubscriptionException e = new SubscriptionException("Subscribe request come too late");
        String reply = DataProviderProtocol.writeSubscribe(e);
        sendReply(requestId, reply);
    }

    private boolean executeUnsubscribe(String itemName, String requestId) throws RemotingException  {
        _log.debug("Processing request: " + requestId);
        String reply = null;
        boolean success = false;
        try {
            _adapter.unsubscribe(itemName);
            reply = DataProviderProtocol.writeUnsubscribe();
            success = true;
        } catch (SubscriptionException | FailureException | Error | RuntimeException e) {
            reply = DataProviderProtocol.writeUnsubscribe(e);
        }
        sendReply(requestId, reply);
        return success;
    }

    private void dummyUnsubscribe(String itemName, String requestId) {
        _log.debug("Skipping request: " + requestId);
        String reply = DataProviderProtocol.writeUnsubscribe();
        sendReply(requestId, reply);
    }

    private static class SubscriptionTask implements Task {
        private SubscribeData _data;
        private String _requestId;
        private DataProviderServerImpl _container;
        public SubscriptionTask(DataProviderServerImpl container, SubscribeData data, String requestId) {
            _container = container;
            _data = data;
            _requestId = requestId;
        }
        @Override
        public final String getCode() {
            return _requestId;
        }
        @Override
        public final boolean doTask() throws RemotingException {
            return _container.executeSubscribe(_data, _requestId);
        }
        @Override
        public final void doLateTask() throws RemotingException {
            _container.refuseLateSubscribe(_data, _requestId);
        }
    }

    private static class UnsubscriptionTask implements Task {
        private String _itemName;
        private String _requestId;
        private DataProviderServerImpl _container;
        public UnsubscriptionTask(DataProviderServerImpl container, String itemName, String requestId) {
            _container = container;
            _itemName = itemName;
            _requestId = requestId;
        }
        @Override
        public final String getCode() {
            return null;
        }
        @Override
        public final boolean doTask() throws RemotingException {
            return _container.executeUnsubscribe(_itemName, _requestId);
        }
        @Override
        public final void doLateTask() {
            _container.dummyUnsubscribe(_itemName, _requestId);
        }
    }
    
    public void sendRemoteCredentials(Map<String,String> credentials) throws RemotingException {
        String notify = DataProviderProtocol.writeRemoteCredentials(credentials);
        NotifySender currNotifySender;
        RequestReceiver currRequestReceiver;
        synchronized (this) {
            currNotifySender = _notifySender;
            currRequestReceiver = _requestReceiver;
        }
        if (currNotifySender != null) {
            currNotifySender.sendNotify(notify);
        }
        if (currRequestReceiver != null) {
            currRequestReceiver.sendUnsolicitedMessage(DataProviderProtocol.AUTH_REQUEST_ID, notify, _log);
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
            if (method.equals(DataProviderProtocol.METHOD_CLOSE)) {
                // this can also precede the init request
                if (! requestId.equals(DataProviderProtocol.CLOSE_REQUEST_ID)) {
                    throw new RemotingException("Unexpected id found while parsing a " + DataProviderProtocol.METHOD_CLOSE + " request");
                }
                final Map<String, String> closeParams = DataProviderProtocol.readClose(request.substring(sep + 1));
                String closeReason = closeParams.get(DataProviderProtocol.KEY_CLOSE_REASON);
                dispose();
                if (closeReason != null) {
                    throw new RemotingException("Close requested by the counterpart with reason: " + closeReason);
                } else {
                    throw new RemotingException("Close requested by the counterpart");
                }
            }

            boolean isInitRequest = method.equals(DataProviderProtocol.METHOD_DATA_INIT);
            if (isInitRequest && !_initExpected) {
                throw new RemotingException("Unexpected late " + DataProviderProtocol.METHOD_DATA_INIT + " request");
            } else if (!isInitRequest && _initExpected) {
                throw new RemotingException("Unexpected request " + request + " while waiting for a " + DataProviderProtocol.METHOD_DATA_INIT + " request");
            }

            if (isInitRequest) {
                _log.debug("Processing request: " + requestId);
                _initExpected = false;
                String keepaliveHint = null;
                String reply;
                Map<String,String> initParams = DataProviderProtocol.readInit(request.substring(sep + 1));
                try {
                    String proxyVersion = initParams.get(PROTOCOL_VERSION_PARAM);
                    String advertisedVersion = getSupportedVersion(proxyVersion);
                        // this may prevent the initialization

                    // we can support multiple versions based on the request of the counterparty
                    // and the version for this connection is indicated by advertisedVersion, 
                    // but currently we only support the latest version
                    assert (advertisedVersion.equals(_maxVersion));

                    keepaliveHint = initParams.get(KEEPALIVE_HINT_PARAM);
                    if (keepaliveHint == null) {
                        keepaliveHint = "0";
                    }
                    initParams.remove(PROTOCOL_VERSION_PARAM);
                    initParams.remove(KEEPALIVE_HINT_PARAM);
                    // the version and keepalive hint are internal parameters, not to be sent to the custom Adapter

                    Iterator<String> paramIter = _adapterParams.keySet().iterator();
                    while (paramIter.hasNext()) {
                        String param = paramIter.next();
                        initParams.put(param, _adapterParams.get(param));
                    }
                    _adapter.init(initParams, _adapterConfig);
                    _adapter.setListener(this);

                    Map<String,String> _proxyParams = new HashMap<>();
                    _proxyParams.put(PROTOCOL_VERSION_PARAM, advertisedVersion);
                    reply = DataProviderProtocol.writeInit(_proxyParams);
                } catch (DataProviderException | VersionException | Error | RuntimeException e) {
                    reply = DataProviderProtocol.writeInit(e);
                }
                useKeepaliveHint(keepaliveHint);
                sendReply(requestId, reply);

            } else if (method.equals(DataProviderProtocol.METHOD_SUBSCRIBE)) {
                SubscribeData data = DataProviderProtocol.readSubscribe(request.substring(sep + 1));
                Task task = new SubscriptionTask(this, data, requestId);
                _helper.doSubscription(data.itemName, task);

            } else if (method.equals(DataProviderProtocol.METHOD_UNSUBSCRIBE)) {
                String itemName = DataProviderProtocol.readUnsubscribe(request.substring(sep + 1));
                Task task = new UnsubscriptionTask(this, itemName, requestId);
                _helper.doUnsubscription(itemName, task);

            } else {
                _log.warn("Discarding unknown request: " + request);
            }

        } catch (RemotingException e) {
            onException(e);
        }
    }

    @Override
    public boolean handleIOException(IOException exception) {
        _log.error("Caught exception: " + exception.getMessage() + ", trying to notify a failure...", exception);
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
        _log.error("Caught exception: " + exception.getMessage() + ", trying to notify a failure...", exception);

        String notify;
        try {
            notify = DataProviderProtocol.writeFailure(exception);
        } catch (RemotingException e) {
            // we cannot give up for encoding issues
            notify = DataProviderProtocol.writeFailure();
        }
        sendNotify(notify);
        return false;
    }

    // ////////////////////////////////////////////////////////////////////////
    // ItemEventListener methods

    public final void update(String itemName, ItemEvent itemEvent, boolean isSnapshot) {
        // both getSubscriptionCode and sendNotify take simple locks,
        // which don't block and don't take further locks;
        // hence this invocation can be made by the Adapter while holding
        // the lock on the item state, with no issues
        String code = _helper.getSubscriptionCode(itemName);
        if (code != null) {
            try {
                String notify = DataProviderProtocol.writeUpdateByEvent(itemName, code, itemEvent, isSnapshot);
                sendNotify(notify);

            } catch (RemotingException e) {
                onException(e);
            }
        } else {
            // there is no active subscription in this moment;
            // this must be an error by the Adapter, which must have sent
            // the event after the termination of an unsubscribe()
            // (or before, but without synchronizing, which is also wrong)
            _log.warn("Unexpected update for item " + itemName);
        }
    }

    public final void update(String itemName, Map<String,?> itemEvent, boolean isSnapshot) {
        // both getSubscriptionCode and sendNotify take simple locks,
        // which don't block and don't take further locks;
        // hence this invocation can be made by the Adapter while holding
        // the lock on the item state, with no issues
        String code = _helper.getSubscriptionCode(itemName);
        if (code != null) {
            try {
                String notify = DataProviderProtocol.writeUpdateByMap(itemName, code, itemEvent, isSnapshot);
                sendNotify(notify);

            } catch (RemotingException e) {
                onException(e);
            }
        } else {
            // there is no active subscription in this moment;
            // this must be an error by the Adapter, which must have sent
            // the event after the termination of an unsubscribe()
            // (or before, but without synchronizing, which is also wrong)
            _log.warn("Unexpected update for item " + itemName);
        }
    }

    public final void update(String itemName, IndexedItemEvent itemEvent, boolean isSnapshot) {
        // both getSubscriptionCode and sendNotify take simple locks,
        // which don't block and don't take further locks;
        // hence this invocation can be made by the Adapter while holding
        // the lock on the item state, with no issues
        String code = _helper.getSubscriptionCode(itemName);
        if (code != null) {
            try {
                String notify = DataProviderProtocol.writeUpdateByIndexedEvent(itemName, code, itemEvent, isSnapshot);
                sendNotify(notify);

            } catch (RemotingException e) {
                onException(e);
            }
        } else {
            // there is no active subscription in this moment;
            // this must be an error by the Adapter, which must have sent
            // the event after the termination of an unsubscribe()
            // (or before, but without synchronizing, which is also wrong)
            _log.warn("Unexpected update for item " + itemName);
        }
    }

    public final void endOfSnapshot(String itemName) {
        // both getSubscriptionCode and sendNotify take simple locks,
        // which don't block and don't take further locks;
        // hence this invocation can be made by the Adapter while holding
        // the lock on the item state, with no issues
        String code = _helper.getSubscriptionCode(itemName);
        if (code != null) {
            try {
                String notify = DataProviderProtocol.writeEndOfSnapshot(itemName, code);
                sendNotify(notify);

            } catch (RemotingException e) {
                onException(e);
            }
        } else {
            // there is no active subscription in this moment;
            // this must be an error by the Adapter, which must have sent
            // the event after the termination of an unsubscribe()
            // (or before, but without synchronizing, which is also wrong)
            _log.warn("Unexpected end of snapshot notify for item " + itemName);
        }
    }

    public final void clearSnapshot(String itemName) {
        // both getSubscriptionCode and sendNotify take simple locks,
        // which don't block and don't take further locks;
        // hence this invocation can be made by the Adapter while holding
        // the lock on the item state, with no issues
        String code = _helper.getSubscriptionCode(itemName);
        if (code != null) {
            try {
                String notify = DataProviderProtocol.writeClearSnapshot(itemName, code);
                sendNotify(notify);

            } catch (RemotingException e) {
                onException(e);
            }
        } else {
            // there is no active subscription in this moment;
            // this must be an error by the Adapter, which must have sent
            // the event after the termination of an unsubscribe()
            // (or before, but without synchronizing, which is also wrong)
            _log.warn("Unexpected clear snapshot request for item " + itemName);
        }
    }

    public final void declareFieldDiffOrder(String itemName, Map<String,DiffAlgorithm[]> algorithmsMap) {
        // both getSubscriptionCode and sendNotify take simple locks,
        // which don't block and don't take further locks;
        // hence this invocation can be made by the Adapter while holding
        // the lock on the item state, with no issues
        String code = _helper.getSubscriptionCode(itemName);
        if (code != null) {
            try {
                String notify = DataProviderProtocol.writeDeclareFieldDiffOrder(itemName, code, algorithmsMap);
                sendNotify(notify);

            } catch (RemotingException e) {
                onException(e);
            }
        } else {
            // there is no active subscription in this moment;
            // this must be an error by the Adapter, which must have sent
            // the event after the termination of an unsubscribe()
            // (or before, but without synchronizing, which is also wrong)
            _log.warn("Unexpected diff algorithms declaration for item " + itemName);
        }
    }

    public final void failure(Exception exception) {
        String notify;
        try {
            notify = DataProviderProtocol.writeFailure(exception);
        } catch (RemotingException e) {
            // we cannot give up for encoding issues
            notify = DataProviderProtocol.writeFailure();
        }
        sendNotify(notify);
    }

}