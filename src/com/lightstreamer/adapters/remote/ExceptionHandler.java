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

import javax.annotation.Nonnull;


/** 
 * Interface to be implemented in order to provide a Remote Server instance with
 * a custom handler for error conditions occurring on the Remote Server.
 * <BR>Note that multiple redundant invocations on the same Remote Server
 * instance are possible.
 */
public interface ExceptionHandler {

    /** 
     * Called by the Remote Server upon a read or write operation
     * failure. This may mean that the connection to Lightstreamer Server
     * is lost; in any way, after this error, the correct operation
     * of this Remote Server instance is compromised.
     * This may be the signal of a normal termination of Lightstreamer Server.
     * If this is not the case, then this Remote Server instance should be closed
     * and a new one should be created and initialized. This may mean
     * closing and restarting the process or just creating a new instance,
     * depending on the implementation choice. This will be
     * detected by the Proxy Adapter, which will react accordingly.
     * <BR>The default handling just terminates the process.
     * 
     * @param exception An Exception showing the cause of the
     * problem.
     * @return true to enable the default handling, false to suppress it.
     */
    boolean handleIOException(@Nonnull IOException exception);

    /** 
     * Called by the Remote Server upon an unexpected error.
     * After this error, the correct operation of this Remote Server
     * instance is compromised.
     * If this is the case, then this Remote Server instance should be closed
     * and a new one should be created and initialized. This may mean
     * closing and restarting the process or just creating a new instance,
     * depending on the implementation choice. This will be
     * detected by the Proxy Adapter, which will react accordingly.
     * <BR>The default handling, in case of a Remote Data Adapter,
     * issues an asynchronous failure notification to the Proxy Adapter.
     * In case of a Remote Metadata Adapter, the default handling ignores
     * the notification; however, as a consequence of the Remote Protocol
     * being broken, the Proxy Adapter may return exceptions against
     * one or more specific requests by Lightstreamer Kernel.
     * 
     * @param exception An Exception showing the cause of the
     * problem.
     * @return true to enable the default handling, false to suppress it.
    */
    boolean handleException(@Nonnull RemotingException exception);

}