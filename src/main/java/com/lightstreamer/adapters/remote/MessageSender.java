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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.lightstreamer.log.LogManager;
import com.lightstreamer.log.Logger;

class MessageSender {
    private static final Logger _replog = LogManager.getLogger("com.lightstreamer.adapters.remote.RequestReply.replies");
    private static final Logger _notlog = LogManager.getLogger("com.lightstreamer.adapters.remote.RequestReply.notifications");
    private static final Logger _repKlog = LogManager.getLogger("com.lightstreamer.adapters.remote.RequestReply.replies.keepalives");
    private static final Logger _notKlog = LogManager.getLogger("com.lightstreamer.adapters.remote.RequestReply.notifications.keepalives");

    private static final String END_LINE = "\r\n";
    private static final String STOP_WAITING_PILL = "STOP_WAITING_PILL";
    private static final String KEEPALIVE_PILL = "KEEPALIVE_PILL";
    
    private final String _name;

    private final BlockingDeque<String> _queue = new LinkedBlockingDeque<String>();
    private final OutputStreamWriter _writer;
    private final WriteState _writeState;
    private final boolean _forReplies;
    private volatile int _keepaliveMillis;

    private ExceptionListener _exceptionListener;

    private boolean _stop;

    public static class WriteState {
        MessageSender lastWriter = null;
    }

    public MessageSender(String name, OutputStream stream, WriteState sharedWriteState, int keepaliveMillis, ExceptionListener exceptionListener) {
        this(name, stream, sharedWriteState, false, keepaliveMillis, exceptionListener);
    }

    public MessageSender(String name, OutputStream stream, WriteState sharedWriteState, boolean forReplies, int keepaliveMillis, ExceptionListener exceptionListener) {
        _name = name;

        _writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        if (sharedWriteState != null) {
            _writeState = sharedWriteState;
        } else {
            _writeState = new WriteState();
        }

        _forReplies = forReplies;
        _keepaliveMillis = keepaliveMillis;

        _exceptionListener = exceptionListener;
        
        _stop = false;
    }
    
    public void changeKeepalive(int keepaliveMillis, boolean alsoInterrupt) {
        _keepaliveMillis = keepaliveMillis;
        if (alsoInterrupt) {
            try {
                // interrupts the current wait as though a keepalive were needed;
                // in most cases, this keepalive will be redundant
                _queue.putFirst(KEEPALIVE_PILL);
            } catch (InterruptedException e) {
            }
        }
    }

    private Logger getProperLogger() {
        return _forReplies ? _replog : _notlog;
    }

    private Logger getProperKeepaliveLogger() {
        return _forReplies ? _repKlog : _notKlog;
    }

    private String getProperType() {
        return _forReplies ? "Reply" : "Notify";
    }

    public final void startOut() {
        Thread t = new Thread() {
            public void run() {
                doRun();
            }
        };
        t.start();
    }

    public final void doRun() {
        getProperLogger().info(getProperType() + " sender '" + _name + "' starting...");

        while (!_stop) { //might as well be while(true)
            
            String msg;
            try {
                if (_keepaliveMillis > 0) {
                    msg = _queue.pollFirst(_keepaliveMillis, TimeUnit.MILLISECONDS);
                } else {
                    msg = _queue.takeFirst();
                }
                //}
                
            } catch (InterruptedException e) {
                _exceptionListener.onException(new RemotingException("Exception caught while waiting on the " + getProperType().toLowerCase() + " queue: " + e.getMessage(), e));
                break;
            }
            
            if (msg == STOP_WAITING_PILL) {
                break;
            }
 
            if (msg == null) {
                synchronized (_writeState) {
                    if (_writeState.lastWriter == null || _writeState.lastWriter == this) {
                        msg = KEEPALIVE_PILL;
                    } else {
                        // the stream is shared and someone wrote after our last write;
                        // that stream will be responsible for the next keepalive
                        continue;
                    }
                }
            }

            if (msg == KEEPALIVE_PILL) {
                // the timeout (real or simulated) has fired
                msg = BaseProtocol.METHOD_KEEPALIVE;
                if (getProperKeepaliveLogger().isDebugEnabled()) {
                    getProperKeepaliveLogger().debug(getProperType() + " line: " + msg);
                }
            } else {
                if (getProperLogger().isDebugEnabled()) {
                    getProperLogger().debug(getProperType() + " line: " + msg);
                }
            }
            
            try {
                synchronized (_writeState) {
                    _writer.write(msg);
                    _writer.write(END_LINE);
                    _writer.flush();
                    _writeState.lastWriter = this;
                }
                
            } catch (IOException e) {
                _exceptionListener.onException(new RemotingException("Exception caught while writing on the " + getProperType().toLowerCase() + " stream: " + e.getMessage(), e));
                break;
            }
        }
        
        getProperLogger().info(getProperType() + " sender '" + _name + "' stopped");
        
    }

    public final void quit() {
        _stop = true;
        try {
            //set the stop pill as first thing
            _queue.putFirst(STOP_WAITING_PILL);
        } catch (InterruptedException e) {
        }
    }

    public final void sendMessage(String msg) {
        if (!_forReplies) {
            long millis = new Date().getTime(); 

            StringBuilder timedNotify = new StringBuilder();
            timedNotify.append(millis);
            timedNotify.append(RemotingProtocol.SEP);
            timedNotify.append(msg);

            msg = timedNotify.toString();
        }

        //enqueue
        try {
            _queue.putLast(msg);
        } catch (InterruptedException e) {
        }

    }
}
