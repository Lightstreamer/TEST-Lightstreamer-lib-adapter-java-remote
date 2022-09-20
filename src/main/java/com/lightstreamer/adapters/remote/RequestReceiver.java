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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.lightstreamer.log.LogManager;
import com.lightstreamer.log.Logger;

class RequestReceiver {
    private static Logger _log = LogManager.getLogger("com.lightstreamer.adapters.remote.RequestReply.requests");

    private String _name;

    private LineNumberReader _reader;

    private NotifySender _replySender;

    private RequestListener _requestListener;
    private ExceptionListener _exceptionListener;

    private volatile boolean _stop;

    public RequestReceiver(String name, InputStream requestStream, OutputStream replyStream, int keepaliveMillis, RequestListener requestListener, ExceptionListener exceptionListener) {
        _name = name;

        _reader = new LineNumberReader(new InputStreamReader(requestStream, StandardCharsets.UTF_8));

        _replySender = new NotifySender(name, replyStream, true, keepaliveMillis, exceptionListener);

        _requestListener = requestListener;
        _exceptionListener = exceptionListener;

        _stop = false;
    }

    public void changeKeepalive(int keepaliveMillis, boolean alsoInterrupt) {
        _replySender.changeKeepalive(keepaliveMillis, alsoInterrupt);
    }

    public final void start() {
        _replySender.start();

        Thread t = new Thread() {
            public void run() {
                doRun();
            }
        };
        t.start();
    }

    public final void doRun() {
        _log.info("Request receiver '" + _name + "' starting...");

        while (!_stop) {
            
            String line = null;
            
            try {
                line = _reader.readLine();
                
                if (_log.isDebugEnabled()) {
                    _log.debug("Request line: " + line);
                }
                
            } catch (IOException e) {
                if (_stop) {
                    break;
                }

                _exceptionListener.onException(new RemotingException("Exception caught while reading from the request stream: " + e.getMessage(), e));
                break;
            }

            if (_stop) {
                break;
            }

            if (line == null) {
                _exceptionListener.onException(new RemotingException("Unexpected end of request stream reached", new EOFException()));
                break;
            }

            onRequestReceived(line);

        } 

        _log.info("Request receiver '" + _name + "' stopped");
    }

    public final void quit() {
        _stop = true;

        _replySender.quit();
    }

    public final void sendReply(String requestId, String reply, Logger properLogger) {
        StringBuilder identifiedReply = new StringBuilder();
        identifiedReply.append(requestId);
        identifiedReply.append(RemotingProtocol.SEP);
        identifiedReply.append(reply);

        reply = identifiedReply.toString();
        properLogger.debug("Processed request: " + requestId);

        _replySender.sendNotify(reply);
    }

    public final void sendUnsolicitedMessage(String virtualRequestId, String msg, Logger properLogger) {
        StringBuilder identifiedReply = new StringBuilder();
        identifiedReply.append(virtualRequestId);
        identifiedReply.append(RemotingProtocol.SEP);
        identifiedReply.append(msg);

        msg = identifiedReply.toString();
        properLogger.debug("Sending unsolicited message");

        _replySender.sendNotify(msg);
    }

    public final void sendRemoteRequest(String requestId, String msg, Logger properLogger) {
        StringBuilder identifiedReply = new StringBuilder();
        identifiedReply.append(requestId);
        identifiedReply.append(RemotingProtocol.SEP);
        identifiedReply.append(msg);

        msg = identifiedReply.toString();
        properLogger.debug("Sending remote request: " + requestId);

        _replySender.sendNotify(msg);
    }

    private void onRequestReceived(String request) {
        int sep = request.indexOf(RemotingProtocol.SEP);
        if (sep < 1) {
            _log.warn("Discarding malformed request: " + request);
            return;
        }

        String requestId = request.substring(0, sep);

        _requestListener.onRequestReceived(requestId, request.substring(sep + 1));
    }
}