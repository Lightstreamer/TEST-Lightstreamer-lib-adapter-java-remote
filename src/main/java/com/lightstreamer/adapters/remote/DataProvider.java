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
 * Provides an interface to be implemented by a Remote Data Adapter in order
 * to attach a Data Provider to Lightstreamer.
 * An instance of a Remote Data Adapter is supplied to Lightstreamer
 * through a {@link DataProviderServer} instance.
 * After initialization, Lightstreamer sets itself
 * as the Remote Data Adapter listener, by calling the setListener method. <BR>
 * Data Providers are used by Lightstreamer Kernel to obtain all data to be
 * pushed to the Clients. Any Item requested by a Client must refer to one
 * supplied by the configured Data Adapters. <BR>
 * A Data Provider supplies data in a publish/subscribe way. Lightstreamer
 * asks for data by calling the subscribe and unsubscribe methods for
 * various Items and the Data Adapter sends ItemEvents to its listener
 * in an asynchronous way. <BR>
 * A Data Adapter can also support Snapshot management. Upon subscription
 * to an Item, the current state of the Item data can be sent to the Server
 * before the updates. This allows the Server to maintain the Item state,
 * by integrating the new ItemEvents into the state (in a way that depends
 * on the Item mode) and to make this state available to the Clients. <BR>
 * Note that the interaction between the Server and the Data Adapter and the
 * interaction between the Server and any Client are independent activities.
 * As a consequence, the very first ItemEvents sent by the Data Adapter to
 * the Server for an Item just subscribed to might be processed before the
 * Server starts feeding any client, even the client that caused the
 * subscription to the Item to be invoked;
 * then, such events would not be forwarded to any client.
 * If it is desirable that a client receives all the ItemEvents that have
 * been produced for an Item by the Data Adapter since subscription time,
 * then the support for the Item Snapshot can be leveraged. <BR>
 * Lightstreamer ensures that calls to subscribe and unsubscribe for
 * the same Item will be interleaved, without redundant calls; whenever
 * subscribe throws an exception, the corresponding unsubscribe call is not
 * issued.
*/
public interface DataProvider {

    /** 
     * Called by the Remote Server to provide
     * initialization information to the Data Adapter.
     * The call must not be blocking; any polling cycle or similar must be started in a different thread.
     * If an exception occurs in this method, Lightstreamer Kernel can't complete the startup and must
     * exit. The initialization information can be supplied in different ways, depending on the way the
     * Remote Server is launched. <BR>
     * The call must not be blocking; any polling cycle or similar must be
     * started in a different thread. Any delay in returning from this call
     * will in turn delay the Server initialization.
     * If an exception occurs in this method, Lightstreamer Server can't
     * complete the startup and must exit.
     * 
     * @param parameters a Map object that contains name-value pairs corresponding
     * to the parameters elements supplied for the Data Adapter configuration.
     * Both names and values are represented as String objects. <BR>
     * The parameters can be supplied through the {@link DataProviderServer#setAdapterParams}
     * method of the DataProviderServer instance.
     * More parameters can be added by leveraging the "init_remote" parameter
     * in the Proxy Adapter configuration.
     * 
     * @param configFile
     * The path on the local disk of the Data Adapter configuration file.
     * Can be null if not specified. <BR>
     * The file path can be supplied by assigning the "AdapterConfig" property of the
     * DataProviderServer instance used.
     * 
     * @exception DataProviderException in case an error occurs that prevents the correct 
     * behavior of the Data Adapter.
     * 
     */
    void init(@Nonnull Map<String,String> parameters, @Nullable String configFile) throws DataProviderException;

    /** 
     * Called by the Remote Server to provide
     * a listener to receive the Item Events carrying data and 
     * asynchronous error notifications for Lightstreamer Kernel.
     * The listener is set before any subscribe is called and is never changed.
     * 
     * @param eventListener A listener.
    */
    void setListener(@Nonnull ItemEventListener eventListener);

    /** 
     * Called by Lightstreamer Remote Server to request data for an Item. If the
     * request succeeds, the Remote Data Adapter can start sending an ItemEvent
     * to the listener for any update of the Item value. Before sending the
     * updates, the Remote Data Adapter may optionally send one or more ItemEvents
     * to supply the current Snapshot. <BR>
     * The general rule to be followed for event dispatching is:
     * <pre>
     *     if IsSnapshotAvailable(itemName) == true
     *         SNAP* [EOS] UPD*
     *     else
     *         UPD*</pre>
     * where:<ul>
     * <li>SNAP represents an Update call with the isSnapshot flag set to true</li>
     * <li>EOS represents an EndOfSnapshot call</li>
     * <li>UPD represents an Update call with the isSnapshot flag set to false;<BR>
     * in this case, the special clearSnapshot call can also be issued.</li>
     * </ul>
     * The composition of the snapshot depends on the Mode in which the Item
     * is to be processed. In particular, for MERGE mode, the snapshot
     * consists of one event and the first part of the rule becomes:
     * <pre>
     *     [SNAP] [EOS] UPD*</pre>
     * where a missing snapshot is considered as an empty snapshot. <BR>
     * If an Item can be requested only in RAW mode, then isSnapshotAvailable
     * should always return false; anyway, when an Item is requested in
     * RAW mode, any snapshot is discarded. <BR>
     * Note that calling endOfSnapshot is not mandatory; however, not
     * calling it in DISTINCT or COMMAND mode may cause the server to keep
     * the snapshot and forward it to the clients only after the first
     * non-snapshot event has been received. The same happens for MERGE mode
     * if neither the snapshot nor the endOfSnapshot call are supplied. <BR>
     * Unexpected snapshot events are converted to non-snapshot events
     * (but for RAW mode, where they are ignored); unexpected endOfSnapshot
     * calls are ignored. <BR>
     * The method can be blocking, but, as the Proxy Adapter
     * implements subscribe and unsubscribe asynchronously,
     * subsequent subscribe-unsubscribe-subscribe-unsubscribe requests
     * can still be issued by Lightstreamer Server to the Proxy Adapter.
     * When this happens, the requests may be queued on the Remote Adapter,
     * hence some Subscribe calls may be delayed.
     * 
     * @param itemName Name of an Item.
     * @exception SubscriptionException in case the request cannot be satisfied.
     * 
     * @exception FailureException in case the method execution has caused
     * a severe problem that can compromise future operation of the Data Adapter.
     * 
    */
    void subscribe(@Nonnull String itemName) throws SubscriptionException, FailureException;

    /** 
     * Called by Lightstreamer Kernel through the Remote Server
     * to end a previous request of data for an Item.
     * After the call has returned, no more ItemEvents for the Item
     * should be sent to the listener until requested by a new subscription
     * for the same Item. <BR>
     * The method can be blocking, but, as the Proxy Adapter
     * implements subscribe and unsubscribe asynchronously,
     * subsequent subscribe-unsubscribe-subscribe-unsubscribe requests
     * can still be issued by Lightstreamer Server to the Proxy Adapter.
     * When this happens, the requests may be queued on the Remote Adapter,
     * hence some Subscribe calls may be delayed.
     * 
     * @param itemName Name of an Item.
     * @exception SubscriptionException in case the request cannot be satisfied.
     * 
     * @exception FailureException in case the method execution has caused
     * a severe problem that can compromise future operation of the Data Adapter.
     * 
     */
    void unsubscribe(@Nonnull String itemName) throws SubscriptionException, FailureException;

    /** 
     * Called by Lightstreamer Kernel through the Remote Server
     * to know whether the Data Adapter, after a subscription for an Item, 
     * will send some Snapshot Item Events before sending the updates. An Item Snapshot can be represented 
     * by zero, one or more Item Events, also depending on the Item mode. The decision whether to supply or 
     * not to supply Snapshot information is entirely up to the Data Adapter. <BR>
     * The method should be nonblocking. The availability of the snapshot for an
     * Item should be a known architectural property. When the snapshot, though expected,
     * cannot be obtained at subscription time, then it can only be considered as empty.
     * 
     * @param itemName Name of an Item.
     * @return true if Snapshot information will be sent for this Item before the updates.
     * @exception SubscriptionException in case the Data Adapter is unable to answer to the request.
     * 
    */
    boolean isSnapshotAvailable(@Nonnull String itemName) throws SubscriptionException;
}