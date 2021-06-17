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
 * A Remote Server object which can run a Remote Metadata Adapter and connect it
 * to a Proxy Metadata Adapter running on Lightstreamer Server. <BR>
 * The object should be provided with a MetadataProvider instance
 * and with suitable local initialization parameters and established
 * connections, then activated through {@link Server#start}
 * and finally disposed through {@link Server#close}.
 * If any preliminary initialization on the supplied MetadataProvider
 * implementation object has to be performed, it should be done through
 * a custom, dedicated method before invoking {@link Server#start}.
 * Further reuse of the same instance is not supported. <BR>
 * By default, the invocations to the Metadata Adapter methods will be
 * done in an unlimited thread pool. A thread pool maximum size can be
 * specified through the custom "lightstreamer.metadata.pool.size" system
 * property. A size of 1 enforces strictly sequential invocations and
 * can be used if parallelization of the calls is not supported by the
 * Metadata Adapter. A size of 0 or negative also implies an unlimited
 * thread pool. <BR>
 * Note that requests with an implicit ordering, like notifyNewSession
 * and NotifySessionClose for the same session, are always guaranteed
 * to be sequentialized in the right way, although they may not occur
 * in the same thread.
 */
public class MetadataProviderServer extends Server {

    private MetadataProviderServerImpl _impl;

    /** 
     * Creates an empty server still to be configured and started.
     * The Init method of the Remote Adapter will be invoked only upon
     * a Proxy Adapter request.
     * 
     * @throws IllegalArgumentException in case something wrong is supplied
     * in system properties related with Metadata Adapter processing.
     */
    public MetadataProviderServer() {
        _impl = new MetadataProviderServerImpl();
        init(_impl);
    }

    /** 
     * Sets the Remote Metadata Adapter instance to be run.
     * 
     * @param adapter the Remote Metadata Adapter instance to be run.
     */
    public final void setAdapter(@Nonnull MetadataProvider adapter) {
        _impl.setAdapter(adapter);
    }
    /**
     * Gets the configured Metadata Adapter that is running or will run.
     * @return the configured Metadata Adapter
     */
    @Nonnull
    public final MetadataProvider getAdapter() {
        return _impl.getAdapter();
    }

    /** 
     * Sets a Map object to be passed to the init method
     * of the Remote Metadata Adapter, to supply optional parameters. <BR>
     * 
     * The default value is an empty HashMap.
     *
     * @param params the Map to be passed to the init method
     * of the Remote Metadata Adapter
     * 
     * @see MetadataProvider#init(Map, String)
     * 
     */
    public final void setAdapterParams(@Nonnull Map<String,String> params) {
        _impl.setAdapterParams(params);
    }
    /** 
     * Gets the Map object to be passed to the init method
     * of the Remote Metadata Adapter, to supply optional parameters. <BR>
     * 
     * The default value is an empty HashMap.
     * 
     * @return the Map object to be passed to the init method
     * of the Remote Metadata Adapter
     * 
     * @see MetadataProvider#init(Map, String)
     * 
     */
    @Nonnull
    public final Map<String,String> getAdapterParams() {
        return _impl.getAdapterParams();
    }

    /** 
     * Sets the pathname of an optional configuration file for the Remote
     * Metadata Adapter, to be passed to the init method. <BR>
     * 
     * The default value is null.
     * 
     * @param configFile the pathname of an optional configuration file for the Remote
     * Metadata Adapter.
     * 
     * @see MetadataProvider#init(Map, String)
     */
    public final void setAdapterConfig(@Nullable String configFile) {
        _impl.setAdapterConfig(configFile);
    }
    /** 
     * Gets the pathname of an optional configuration file for the Remote
     * Metadata Adapter, to be passed to the init method. <BR>
     * 
     * The default value is null.
     * 
     * @return the pathname of an optional configuration file for the Remote
     * Metadata Adapter
     * 
     * @see MetadataProvider#init(Map, String)
     */
    @Nullable
    public final String getAdapterConfig() {
        return _impl.getAdapterConfig();
    }

}