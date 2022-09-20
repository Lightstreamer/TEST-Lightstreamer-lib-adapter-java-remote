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
 * Used by Lightstreamer to receive the update events and other kinds of events related with
 * the Item subscription lifecycle. It can also receive asynchronous severe error notifications
 * from the Data Adapter. The listener instance is supplied to the Data Adapter by Lightstreamer Kernel
 * (through the Remote Server) through a setListener call.
 * Update events are specified through maps that associate fields and values. Depending on the kind
 * of subscription, the mapping for fields unchanged since the previous update can be omitted.
 * Some alternative methods to supply update events are available, but they have been deprecated.
 * Field values should be expressed as String; the use of byte arrays is also allowed, but it has
 * been deprecated.
 */
public interface ItemEventListener {

    /** 
     * Called by a Data Adapter to send an Item Event to Lightstreamer Kernel when the Item Event is 
     * implemented as an ItemEvent instance.
     *
     * @deprecated The method is deprecated. Use the Map version to supply field values. 
     * 
     * @param itemName The name of the Item whose values are carried by the Item Event.
     * @param itemEvent An ItemEvent instance.
     * @param isSnapshot true if the Item Event carries the Item Snapshot.
    */
    @Deprecated
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
     * A value should be expressed as a String;
     * the use of a byte array, to supply a string encoded in the ISO-8859-1 (ISO-LATIN-1)
     * character set, is also allowed, but it has been deprecated.
     * A Field value can be null or missing 
     * if the Field is not to be reported in the event.
     * @param isSnapshot true if the Item Event carries the Item Snapshot.
    */
    void update(@Nonnull String itemName, @Nonnull Map<String,?> itemEvent, boolean isSnapshot);

    /** 
     * Called by a Data Adapter to send an Item Event to Lightstreamer Kernel when the Item Event is 
     * implemented as an IndexedItemEvent instance.
     * 
     * @deprecated The method is deprecated. Use the Map version to supply field values. 
     * 
     * @param itemName The name of the Item whose values are carried by the Item Event.
     * @param itemEvent An IndexedItemEvent instance.
     * @param isSnapshot true if the Item Event carries the Item Snapshot.
    */
    @Deprecated
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
     * Called by a Data Adapter to send to Lightstreamer kernel Field
     * "diff" support information in relation to a specific subscribed Item.
     * By default, the Server can decide whether or not to try to apply
     * an available algorithm to compute the difference between a value
     * and the previous one in order to send the client this difference,
     * for "delta delivery" purpose. The applicability of an algorithm
     * depends on the capability of the client, the suitability of the
     * values, and the throughput efficiency achieved. 
     * With this method, it is possible to enforce which algorithms to try
     * and in which order, on a field-by-field basis. The available algorithms
     * are specified in the {@link DiffAlgorithm} enumerative class.
     * <BR>The declared "diff" order is a static property of fields, hence
     * it cannot change throughout all the duration of the subscription.
     * For this reason, this method should be invoked before any update is sent.
     * However, since the available Fields may not be fully predetermined
     * and new Fields may be added at any time, it is possible to invoke
     * this method multiple times, interspersed with update events, with
     * a cumulative behavior.
     * <BR>As a consequence, any attempt to declare the "diff" order of a
     * Field for which it has been already specified will be ignored.
     * Moreover, if a Field is used in an update before sending any "diff"
     * order information, it will stick to the Server default, and any
     * subsequent specification of a "diff" order for that field will be ignored.
     * <BR>By specifying "diff" algorithms consistent with the expected values,
     * a significant outbound bandwidth reduction can be achieved on the Server.
     * On the other hand, if unsuitable values are sent (for instance, strings
     * that are not valid JSON representations for a Field with the
     * {@link DiffAlgorithm#JSONPATCH} algorithm specified), a significant
     * computational overhead with no useful effect may be added to the Server.
     *
     * @param itemName  The name of the Item whose "diff" order information
     * is carried by this notification.
     * @param algorithmsMap  A java.util.Map instance, in which Field names are
     * associated to ordered arrays of "diff" algorithms. Omitted fields or
     * null arrays will add no information. On the other hand, an empty array
     * can be supplied to mean that no "diff" algorithm is admitted for a field.
     */
    void declareFieldDiffOrder(@Nonnull String itemName, @Nonnull Map<String,DiffAlgorithm[]> algorithmsMap);

    /** 
     * Called by a Data Adapter to notify Lightstreamer Kernel of the occurrence of a severe problem that 
     * can compromise future operation of the Data Adapter.
     * 
     * @param exception Any Exception object, with the description of the problem.
    */
    void failure(@Nullable Exception exception);
}