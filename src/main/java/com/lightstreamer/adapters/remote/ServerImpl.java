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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.lightstreamer.log.LogManager;
import com.lightstreamer.log.Logger;

abstract class ServerImpl implements RequestListener, ExceptionListener, ExceptionHandler {

    private static final Logger _log = LogManager.getLogger("com.lightstreamer.adapters.remote.Server");

    private static int _number = 0;

    private String _name;

    private InputStream _requestStream;
    private OutputStream _replyStream;
    private OutputStream _notifyStream;
    
    private final Integer _configuredKeepaliveMillis;
    
    private String _remoteUser;
    private String _remotePassword;

    public static final String PROTOCOL_VERSION_PARAM = "ARI.version";
    public static final String KEEPALIVE_HINT_PARAM = "keepalive_hint.millis";
    public static final String USER_PARAM = "user";
    public static final String PASSWORD_PARAM = "password";

    public static final int MIN_KEEPALIVE_MILLIS = 1000;
        // protection limit; it might be made configurable;
        // in that case, if 0 is allowed to suppress keepalives, its handling should be added
    public static final int STRICT_KEEPALIVE_MILLIS = 1000;
        // default to act on both intermediate nodes and the Proxy Adapter,
        // when the Proxy Adapter provides no information
    public static final int DEFAULT_KEEPALIVE_MILLIS = 10000;
        // default to act on intermediate nodes, not on the Proxy Adapter

    private ExceptionHandler _exceptionHandler;

    protected RequestReceiver _requestReceiver;
    protected NotifySender _notifySender;

    public ServerImpl() {
        _number++;
        _name = "#" + _number;

        _requestStream = null;
        _replyStream = null;
        _notifyStream = null;

        _exceptionHandler = null;

        _requestReceiver = null;
        _notifySender = null;

        String keepaliveConf = System.getProperty("lightstreamer.keepalive.millis");
        if (keepaliveConf != null) {
            try {
                int keepaliveMillis = Integer.parseInt(keepaliveConf);
                if (keepaliveMillis >= 0) {
                    _configuredKeepaliveMillis = keepaliveMillis;
                } else {
                    _configuredKeepaliveMillis = 0;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid lightstreamer.keepalive.millis configuration: " + keepaliveConf);
            }
        } else {
            _configuredKeepaliveMillis = null;
        }
    }

    public final void setName(String value) {
        _name = value;
    }
    public final String getName() {
        return _name;
    }

    public final void setRemoteUser(String value) {
        _remoteUser = value;
    }
    public final String getRemoteUser() {
        return _remoteUser;
    }
    public final void setRemotePassword(String value) {
        _remotePassword = value;
    }
    public final String getRemotePassword() {
        return _remotePassword;
    }

    public final void setRequestStream(InputStream value) {
        _requestStream = value;
    }
    public final InputStream getRequestStream() {
        return _requestStream;
    }

    public final void setReplyStream(OutputStream value) {
        _replyStream = value;
    }
    public final OutputStream getReplyStream() {
        return _replyStream;
    }

    public final void setNotifyStream(OutputStream value) {
        _notifyStream = value;
    }
    public final OutputStream getNotifyStream() {
        return _notifyStream;
    }

    public final void setExceptionHandler(ExceptionHandler value) {
        _exceptionHandler = value;
    }
    public final ExceptionHandler getExceptionHandler() {
        return _exceptionHandler;
    }

    protected String getSupportedVersion(String proxyVersion) throws VersionException {
        if (proxyVersion == null) {
            // protocol version 1.8.0 or earlier;
            // we cannot support a lower version;
            // if we supported a version higher than 1.8.x, we should fail here;
            // we currently support 1.8.x
            // this could still be higher and incompatible with the proxy version,
            // but we cannot know that and fail;
            // version advertisement not available here
            return null;
        } else {
            // protocol version specified in proxyVersion (must be 1.8.2 or later);
            // if we supported a lower version, we could advertise it
            // and hope that the proxy supports it as well;
            // if we supported a higher version, we could fail here,
            // but we can still advertise it and let the proxy refuse
            if (proxyVersion.equals("1.8.1")) {
                // temporary version that was used internally but never published
                throw new VersionException("Unsupported reserved protocol version number: " + proxyVersion);
            }
            return "1.8.2";
        }
    }
    
    private Map<String, String> getCredentialParams() {
        if (_remoteUser != null && _remotePassword != null) {
            Map<String, String> _proxyParams = new HashMap<String, String>();
            _proxyParams.put(USER_PARAM, _remoteUser);
            _proxyParams.put(PASSWORD_PARAM, _remotePassword);
            return _proxyParams;
        } else {
            return null;
        }
    }

    protected abstract void sendRemoteCredentials(Map<String,String> credentials) throws RemotingException;
    
    private void changeKeepalive(int keepaliveTime) {
        NotifySender currNotifySender;
        RequestReceiver currRequestReceiver;
        synchronized (this) {
            currNotifySender = _notifySender;
            currRequestReceiver = _requestReceiver;
        }
        if (currNotifySender != null) {
            currNotifySender.changeKeepalive(keepaliveTime, true);
        }
        if (currRequestReceiver != null) {
            currRequestReceiver.changeKeepalive(keepaliveTime, false);
            // interruption not needed, since in this context we are about to reply
        }
    }
    
    protected void useKeepaliveHint(String keepaliveHint) {
        if (keepaliveHint == null) {
            // no information: we stick to a stricter default
            if (_configuredKeepaliveMillis == null) {
                // we had temporarily set the default, but we have to set a final value in any case
                _log.info("Keepalive time for " + _name + " finally set to " + STRICT_KEEPALIVE_MILLIS + " milliseconds" +
                        " to support old Proxy Adapter");
                changeKeepalive(STRICT_KEEPALIVE_MILLIS);
            } else {
                // for backward compatibility we keep the setting;
                // it is possible that the setting is too long
                // and the Proxy Adapter activity check is triggered
            }
        } else {
            int keepaliveTime = Integer.parseInt(keepaliveHint);
            if (keepaliveTime <= 0) {
                // no restrictions, so our default is still meaningful
            } else if (_configuredKeepaliveMillis == null) {
                // we had temporarily set the default, but we have to set a final value in any case
                if (keepaliveTime < DEFAULT_KEEPALIVE_MILLIS) {
                    if (keepaliveTime >= MIN_KEEPALIVE_MILLIS) {
                        _log.info("Keepalive time for " + _name + " finally set to " + keepaliveTime + " milliseconds" +
                                " as per Proxy Adapter suggestion");
                        changeKeepalive(keepaliveTime);
                    } else {
                        _log.warn("Keepalive time for " + _name + " finally set to " + MIN_KEEPALIVE_MILLIS + "milliseconds" +
                                " , despite a Proxy Adapter suggestion of " + keepaliveTime + " milliseconds");
                        changeKeepalive(MIN_KEEPALIVE_MILLIS);
                    }
                } else {
                    // the default setting is stricter, so it's ok
                    _log.info("Keepalive time for " + _name + " finally confirmed to " + DEFAULT_KEEPALIVE_MILLIS + " milliseconds" +
                            " consistently with Proxy Adapter suggestion");
                }
            } else if (_configuredKeepaliveMillis > 0) {
                // we had set the configured value, but we may have to change it
                if (keepaliveTime < _configuredKeepaliveMillis) {
                    if (keepaliveTime >= MIN_KEEPALIVE_MILLIS) {
                        _log.warn("Keepalive time for " + _name + " changed to " + keepaliveTime + " milliseconds" +
                                " as per Proxy Adapter suggestion");
                        changeKeepalive(keepaliveTime);
                    } else {
                        _log.warn("Keepalive time for " + _name + " changed to " + MIN_KEEPALIVE_MILLIS + "milliseconds" +
                                " , despite a Proxy Adapter suggestion of " + keepaliveTime + " milliseconds");
                        changeKeepalive(MIN_KEEPALIVE_MILLIS);
                    }
                } else {
                    // our setting is stricter, so it's ok
                }
            } else {
                // we hadn't used the keepalive, but we may have to enforce them
                if (keepaliveTime >= MIN_KEEPALIVE_MILLIS) {
                    _log.warn("Keepalives for " + _name + " forced with time " + keepaliveTime + " milliseconds" +
                            " as per Proxy Adapter suggestion");
                    changeKeepalive(keepaliveTime);
                } else {
                    _log.warn("Keepalives for " + _name + " forced with time " + MIN_KEEPALIVE_MILLIS + "milliseconds" +
                            " , despite a Proxy Adapter suggestion of " + keepaliveTime + " milliseconds");
                    changeKeepalive(MIN_KEEPALIVE_MILLIS);
                }
            }
        }
    }

    public void start() throws DataProviderException, RemotingException, MetadataProviderException {
        int keepaliveMillis;
        if (_configuredKeepaliveMillis == null) {
            keepaliveMillis = DEFAULT_KEEPALIVE_MILLIS;
            _log.info("Keepalive time for " + _name + " temporarily set to " + keepaliveMillis + " milliseconds");
        } else if (_configuredKeepaliveMillis > 0) {
            keepaliveMillis = _configuredKeepaliveMillis;
            _log.info("Keepalive time for " + _name + " set to " + keepaliveMillis + " milliseconds");
        } else {
            keepaliveMillis = 0;
            _log.info("Keepalives for " + _name + " not set");
        }

        RequestReceiver currRequestReceiver = null;
        currRequestReceiver = new RequestReceiver(_name, _requestStream, _replyStream, keepaliveMillis, this, this);

        NotifySender currNotifySender = null;
        if (_notifyStream != null) {
            currNotifySender = new NotifySender(_name, _notifyStream, keepaliveMillis, this);
        }

        synchronized (this) {
            _notifySender = currNotifySender;
            _requestReceiver = currRequestReceiver;
        }
        
        if (currNotifySender != null) {
            currNotifySender.start();
        }
        currRequestReceiver.start();

        Map<String, String> credentials = getCredentialParams();
        if (credentials != null) {
            sendRemoteCredentials(credentials);
        }
    }

    public final void stop() {
        RequestReceiver currRequestReceiver;
        NotifySender currNotifySender;
        synchronized (this) {
            currRequestReceiver = _requestReceiver;
            _requestReceiver = null;
            currNotifySender = _notifySender;
            _notifySender = null;
        }
        
        if (currRequestReceiver != null) {
            currRequestReceiver.quit();
        }
        if (currNotifySender != null) {
            currNotifySender.quit();
        }

    }

    public final void dispose() {
        if (_requestStream != null) {
            try {
                _requestStream.close();
            } catch (IOException e) {
            }
            _requestStream = null;
        }
        if (_replyStream != null) {
            try {
                _replyStream.close();
            } catch (IOException e) {
            }
            _replyStream = null;
        }
        if (_notifyStream != null) {
            try {
                _notifyStream.close();
            } catch (IOException e) {
            }
            _notifyStream = null;
        }
        // Invokes an hook for subclasses, to give them the opportunity
        // to clean up.
        onDispose();
    }

    protected abstract void onDispose();

    public abstract boolean handleIOException(IOException exception);
    public abstract boolean handleException(RemotingException exception);

    public abstract void onRequestReceived(String requestId, String request);

    public final void onException(RemotingException exception) {
        
        Throwable cause = exception.getCause();
        if ((cause instanceof IOException)/* || (cause instanceof SocketException)*/) {
            if (_exceptionHandler != null) {
                _log.info("Caught exception: " + exception + ", notifying the application...");
                if (!_exceptionHandler.handleIOException((IOException)cause)) {
                    return;
                }
            }
            handleIOException((IOException)cause);
            return;
        }
        

        if (_exceptionHandler != null) {
            _log.info("Caught exception: " + exception + ", notifying the application...");
            if (!_exceptionHandler.handleException(exception)) {
                return;
            }
        }
        handleException(exception);
    }
}