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
 * Used by Lightstreamer Kernel to receive the Item Events and any asynchronous severe error notification 
 * from the Data Adapter. The listener instance is supplied to the Data Adapter by Lightstreamer Kernel
 * (through the Remote Server) 
 * through a setListener call. The listener can manage multiple kinds of Item Events: ItemEvent objects, 
 * IndexedItemEvent objects and Map&lt;String,?&gt; objects. The common characteristics of all these kinds of 
 * Item Event objects are that:
 * <ul>
 * <li>they contain the new values and, in some cases, the current values of the Fields of an Item; the 
 * Item name is not directly asked to the object;</li>
 * <li>they provide an enumerator that supplies the names of all the Fields reported in the Item 
 * Event;</li>
 * <li>they provide a method for getting the value of a Field by name; the value can be expressed either 
 * as a String or as a byte array (the special mandatory fields for COMMAND Mode named "key" and "command" 
 * must be encoded as String).</li>
 * </ul>
 * When an Item Event instance has been sent to the listener, it is totally owned by Lightstreamer 
 * and it must not be anymore changed by the Data Adapter. The Remote Server may also hold the 
 * object for some time after the listener call has returned. When Item Events are implemented as wrappers of 
 * the data objects received from the external feed (like JMS Messages), this has to be considered.
 */
public interface ItemEventListener {

    /** 
     * Called by a Data Adapter to send an Item Event to Lightstreamer Kernel when the Item Event is 
     * implemented as an ItemEvent instance. <BR>
     * The Remote Adapter should ensure that, after an unsubscribe call
     * for the Item has returned, no more update calls are issued, until
     * requested by a new subscription for the same Item.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Remote
     * Server.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     * 
     * @param itemName The name of the Item whose values are carried by the Item Event.
     * @param itemEvent An ItemEvent instance.
     * @param isSnapshot true if the Item Event carries the Item Snapshot.
    */
    void update(@Nonnull String itemName, @Nonnull ItemEvent itemEvent, boolean isSnapshot);

    /** 
     * Called by a Data Adapter to send an Item Event to Lightstreamer Kernel when the Item Event is 
     * implemented as a Map&lt;String,?&gt; instance. <BR>
     * The Remote Adapter should ensure that, after an unsubscribe call
     * for the Item has returned, no more update calls are issued, until
     * requested by a new subscription for the same Item.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Remote
     * Server.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     * 
     * @param itemName The name of the Item whose values are carried by the Item Event.
     * @param itemEvent A Map instance, in which Field names are associated to Field values. 
     * A value can be expressed as either a String or a byte array, the latter case being the most efficient, 
     * though restricted to the ISO-8859-1 (ISO-LATIN-1) character set. A Field value can be null or missing 
     * if the Field is not to be reported in the event.
     * @param isSnapshot true if the Item Event carries the Item Snapshot.
    */
    void update(@Nonnull String itemName, @Nonnull Map<String,?> itemEvent, boolean isSnapshot);

    /** 
     * Called by a Data Adapter to send an Item Event to Lightstreamer Kernel when the Item Event is 
     * implemented as an IndexedItemEvent instance. <BR>
     * The Remote Adapter should ensure that, after an unsubscribe call
     * for the Item has returned, no more update calls are issued, until
     * requested by a new subscription for the same Item.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Remote
     * Server.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     * 
     * @param itemName The name of the Item whose values are carried by the Item Event.
     * @param itemEvent An IndexedItemEvent instance.
     * @param isSnapshot true if the Item Event carries the Item Snapshot.
    */
    void update(@Nonnull String itemName, @Nonnull IndexedItemEvent itemEvent, boolean isSnapshot);

    /** 
     * Called by a Data Adapter to signal to Lightstreamer Kernel that no more Item Event belonging to the 
     * Snapshot are expected for an Item. This call is optional, because the Snapshot completion can also be 
     * inferred from the isSnapshot flag in the update calls. However, this call allows 
     * Lightstreamer Kernel to be informed of the Snapshot completion before the arrival of the first 
     * non-snapshot event. Calling this function is recommended if the Item is to be subscribed in DISTINCT 
     * mode. In case the Data Adapter returned false to isSnapshotAvailable for the same Item, this function 
     * should not be called. <BR>
     * The Remote Adapter should ensure that, after an unsubscribe call
     * for the Item has returned, a possible pending endOfSnapshot call related
     * with the previous subscription request is no longer issued.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Remote
     * Server.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     * 
     * @param itemName The name of the Item whose snapshot has been completed.
    */
    void endOfSnapshot(@Nonnull String itemName);

    /** 
     * Called by a Data Adapter to signal to Lightstreamer Kernel that the
     * current Snapshot of the Item has suddenly become empty. More precisely:
     * <ul>
     * <li>for subscriptions in MERGE mode, the current state of the Item will
     * be cleared, as though an update with all fields valued as null were issued;</li>
     * <li>for subscriptions in COMMAND mode, the current state of the Item
     * will be cleared, as though a DELETE event for each key were issued;</li>
     * <li>for subscriptions in DISTINCT mode, a suitable notification that
     * the Snapshot for the Item should be cleared will be sent to all the
     * clients currently subscribed to the Item (clients based on some old
     * client library versions may not be notified); at the same time,
     * the current recent update history kept by the Server for the Item
     * will be cleared and this will affect the Snapshot for new subscriptions;</li>
     * <li>for subscriptions in RAW mode, there will be no effect.</li>
     * </ul>
     * Note that this is a real-time event, not a Snapshot event; hence,
     * in order to issue this call, it is not needed that the Data Adapter
     * has returned true to isSnapshotAvailable for the specified Item;
     * moreover, if invoked while the Snapshot is being supplied, the Kernel
     * will infer that the Snapshot has been completed. <BR>
     * The Adapter should ensure that, after an unsubscribe call for the
     * Item has returned, a possible pending clearSnapshot call related with
     * the previous subscription request is no longer issued.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Kernel.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     * 
     * @param itemName The name of the Item whose Snapshot has become empty.
    */
    void clearSnapshot(@Nonnull String itemName);

    /** 
     * Called by a Data Adapter to notify Lightstreamer Kernel of the occurrence of a severe problem that 
     * can compromise future operation of the Data Adapter.
     * 
     * @param exception Any Excetion object, with the description of the problem.
    */
    void failure(@Nullable Exception exception);
}