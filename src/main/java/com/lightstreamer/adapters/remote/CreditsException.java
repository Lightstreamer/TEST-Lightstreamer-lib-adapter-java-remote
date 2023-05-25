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

import javax.annotation.Nullable;

/** 
 * Thrown by the notify* methods in MetadataProvider if some functionality cannot be allowed 
 * to the supplied User. This may occur if the user is not granted some resource or if the user 
 * would exceed the granted amount. Different kinds of problems can be distinguished by an error code. 
 * Both the error message detail and the error code will be forwarded by Lightstreamer Kernel to the Client.
 */
public class CreditsException extends MetadataException {
    private int _clientErrorCode;
    private String _clientErrorMsg;

    /** 
     * Constructs a CreditsException with supplied error code and message text. 
     *
     * @param clientErrorCode Error code that can be used to distinguish the kind of problem. It must
     * be a negative integer, or zero to mean an unspecified problem.
     * @param msg The detail message.
     */
    public CreditsException(int clientErrorCode, @Nullable String msg) {
        super(msg);
        _clientErrorCode = clientErrorCode;
    }

    /** 
     * Constructs a CreditsException with supplied error code and message text to be forwarded to the Client. 
     * An internal error message text can also be specified. 
     *
     * @param clientErrorCode Error code that can be used to distinguish the kind of problem. It must 
     * be a negative integer, or zero to mean an unspecified problem.
     * @param msg The detail message.
     * @param userMsg A detail message to be forwarded to the Client. It can
     * be null, in which case an empty string message will be assumed.
     * The message is free, but if it is not in simple ASCII or if it is
     * multiline, it might be altered in order to be sent to very old
     * non-TLCP clients.
     */
    public CreditsException(int clientErrorCode, @Nullable String msg, @Nullable String userMsg) {
        super(msg);
        _clientErrorCode = clientErrorCode;
        _clientErrorMsg = userMsg;
    }

    /** 
     * Gets the error code to be forwarded to the client.
     * @return the error code to be forwarded to the client.
     */
    public final int getClientErrorCode() {
        return _clientErrorCode;
    }

    /** 
     * Gets the error detail message to be forwarded to the client.
     * @return error detail message to be forwarded to the client.
     */
    @Nullable
    public final String getClientErrorMsg() {
        return _clientErrorMsg;
    }
}