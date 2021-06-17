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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/** 
 * A Remote Server object which can run a Remote Data Adapter and connect it
 * to a Proxy Data Adapter running on Lightstreamer Server. <BR>
 * The object should be provided with a DataProvider instance
 * and with suitable local initialization parameters and established
 * connections, then activated through {@link Server#start}
 * and finally disposed through {@link Server#close}.
 * If any preliminary initialization on the supplied DataProvider
 * implementation object has to be performed, it should be done through
 * a custom, dedicated method before invoking {@link Server#start}.
 * Further reuse of the same instance is not supported. <BR>
 * By default, the invocations to the Data Adapter methods will be
 * done in an unlimited thread pool. A thread pool maximum size can be
 * specified through the custom "lightstreamer.data.pool.size" system
 * property; a size of 0 or negative also implies an unlimited thread
 * pool. <BR>
 * Note that Subscribe and Unsubscribe invocations for the same item
 * are always guaranteed to be sequentialized in the right way,
 * although they may not occur in the same thread.
 */
public class DataProviderServer extends Server {

    private DataProviderServerImpl _impl;

    /** 
     * Creates an empty server still to be configured and started.
     * The init method of the Remote Adapter will be invoked only upon
     * a Proxy Adapter request.
     * 
     * @throws IllegalArgumentException in case something wrong is supplied
     * in system properties related with Data Adapter processing.
     */
    public DataProviderServer() {
        _impl = new DataProviderServerImpl();
        init(_impl);
    }

    /** 
     * Sets the Remote Data Adapter instance to be run.
     * 
     * @param adapter the Remote Data Adapter instance to be run.
     */
    public final void setAdapter(@Nonnull DataProvider adapter) {
        _impl.setAdapter(adapter);
    }
    /**
     * Gets the configured Data Adapter that is running or will run.
     * @return the configured Data Adapter
     */
    @Nonnull
    public final DataProvider getAdapter() {
        return _impl.getAdapter();
    }

    /** 
     * Sets a Map object to be passed to the init method
     * of the Remote Data Adapter, to supply optional parameters. <BR>
     * 
     * The default value is an empty HashMap.
     *
     * @param params the Map to be passed to the init method
     * of the Remote Data Adapter
     * 
     * @see DataProvider#init(Map, String)
     * 
     */
    public final void setAdapterParams(@Nonnull Map<String,String> params) {
        _impl.setAdapterParams(params);
    }
    /** 
     * Gets the Map object to be passed to the init method
     * of the Remote Data Adapter, to supply optional parameters. <BR>
     * 
     * The default value is an empty HashMap.
     * 
     * @return the Map object to be passed to the init method
     * of the Remote Data Adapter
     * 
     * @see DataProvider#init(Map, String)
     * 
     */
    @Nonnull
    public final Map<String,String> getAdapterParams() {
        return _impl.getAdapterParams();
    }

    /** 
     * Sets the pathname of an optional configuration file for the Remote
     * Data Adapter, to be passed to the init method. <BR>
     * 
     * The default value is null.
     * 
     * @param configFile the pathname of an optional configuration file for the Remote
     * Data Adapter.
     * 
     * @see DataProvider#init(Map, String)
     */
    public final void setAdapterConfig(@Nullable String configFile) {
        _impl.setAdapterConfig(configFile);
    }
    /** 
     * Gets the pathname of an optional configuration file for the Remote
     * Data Adapter, to be passed to the init method. <BR>
     * 
     * The default value is null.
     * 
     * @return the pathname of an optional configuration file for the Remote
     * Data Adapter
     * 
     * @see DataProvider#init(Map, String)
     */
    @Nullable
    public final String getAdapterConfig() {
        return _impl.getAdapterConfig();
    }

}