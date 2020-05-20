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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Issued by the Remote Server upon an unexpected error.
 * It is used both synchronously (thrown, for instance,
 * upon {@link com.lightstreamer.adapters.remote.Server#start})
 * and asynchronously (through an invocation
 * of {@link com.lightstreamer.adapters.remote.ExceptionHandler#handleException}).
 */
public class RemotingException extends Exception {

    /**
     * Used by the Remote Server to construct a RemotingException
     * with a supplied error message text.
     *
     * @param msg The detail message.
     */
    public RemotingException(@Nullable String msg) {
        super(msg);
    }

    /**
     * Used by the Remote Server to construct a RemotingException
     * with a supplied error message text and cause exception.
     *
     * @param msg The detail message.
     * @param e The cause.
     */
    public RemotingException(@Nullable String msg, @Nonnull Exception e) {
        super(msg, e);
    }
}