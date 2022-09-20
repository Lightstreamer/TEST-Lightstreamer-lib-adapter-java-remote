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
 * Used by MetadataProvider to provide value objects to the calls
 * to methods NotifyNewTables, NotifyTablesClose, and
 * notifyMpnSubscriptionActivation.
 * The attributes of every Table (i&#46;e&#46;: Subscription) to be added or removed
 * to a Session have to be written to a TableInfo instance.
 */
public class TableInfo {
    private int _winIndex;
    private Mode _mode;
    private String _group;
    private String _dataAdapter;
    private String _schema;
    private int _min;
    private int _max;
    private String _selector;
    private String[] _itemNames;
    private SubscriptionStatistics[] _statistics;

    /** 
     * Used by Lightstreamer to create a TableInfo instance,
     * collecting the various attributes of a Table (i&#46;e&#46;: Subscription).
     * 
     * @param winIndex Unique identifier of the client subscription request within the session.
     * @param mode Publishing Mode for the Items in the Table (i&#46;e&#46;: Subscription)
     * (it must be the same across all the Table).
     * @param group The name of the Item Group (or specification of the Item List)
     * to which the subscribed Items belong.
     * @param dataAdapter The name of the Data Adapter to which the Table (i&#46;e&#46;: Subscription) refers.
     * @param schema The name of the Field Schema (or specification of the Field List)
     * used for the subscribed Items.
     * @param min The 1-based index of the first Item in the Group to be considered in the 
     * Table (i&#46;e&#46;: Subscription).
     * @param max The 1-based index of the last Item in the Group to be considered in the 
     * Table (i&#46;e&#46;: Subscription).
     * @param selector The name of the optional Selector associated to the table (i&#46;e&#46;: Subscription).
     * @param itemNames  The array of Item names involved in this Table (i&#46;e&#46;: Subscription).
     * @param statistics  The array of subscription statistics associated to the subscribed Items.
     * It can be null if the statistics are not available; otherwise it should have the same size
     * as the itemNames array.
    */
    public TableInfo(int winIndex, @Nonnull Mode mode, @Nonnull String group, @Nonnull String dataAdapter, @Nonnull String schema, int min, int max, @Nullable String selector, @Nonnull String[] itemNames, @Nullable SubscriptionStatistics[] statistics) {
        _winIndex = winIndex;
        _mode = mode;
        _group = group;
        _dataAdapter = dataAdapter;
        _schema = schema;
        _min = min;
        _max = max;
        _selector = selector;
        _itemNames = itemNames;
        _statistics = statistics;
    }

    /** 
     * Gets the unique identifier of the client subscription request within the session.
     * This allows for matching the corresponding subscription and unsubscription requests.
     * Note that, for clients based on a very old version of a client library
     * or text protocol, subscription requests may involve multiple Tables
     * (i&#46;e&#46;: Subscriptions), hence multiple objects of this type can be supplied
     * in a single array by MetadataProvider through NotifyNewTables and
     * NotifyTablesClose. In this case, the value returned
     * is the same for all these objects and the single Tables (i&#46;e&#46;: Subscriptions)
     * can be identified by their relative position in the array.
     * 
     * @return the unique identifier of the client subscription request.
     */
    public final int getWinIndex() {
        return _winIndex;
    }

    /** 
     * Gets the publishing Mode for the Items in the Table (i&#46;e&#46;: Subscription)
     * (it must be the same across all the Table). 
     * 
     * @return the publishing Mode for the Items in the Table
     */
    @Nonnull
    public final Mode getMode() {
        return _mode;
    }

    /** 
     * Gets the name of the Item Group (or specification of the Item List)
     * to which the subscribed Items belong. 
     * 
     * @return the name of the Item Group (or specification of the Item List)
     */
    @Nonnull
    public final String getId() {
        return _group;
    }

    /**
     * Returns the name of the Data Adapter to which the Table (i&#46;e&#46;: Subscription) refers.
     *
     * @return the name of the involved Data Adapter.
     */
    @Nonnull
    public String getDataAdapter() {
        return _dataAdapter;
    }
    
    /** 
     * Gets the name of the Field Schema (or specification of the Field List)
     * used for the subscribed Items.
     * 
     * @return the name of the Field Schema (or specification of the Field List)
     */
    @Nonnull
    public final String getSchema() {
        return _schema;
    }

    /** 
     * Gets the index of the first Item in the Group
     * to be considered in the Table (i&#46;e&#46;: Subscription).
     * 
     * @return the index of the first Item in the Group
     */
    public final int getMin() {
        return _min;
    }

    /** 
     * Gets the index of the last Item in the Group
     * to be considered in the Table (i&#46;e&#46;: Subscription).
     * 
     * @return  the index of the last Item in the Group
     */
    public final int getMax() {
        return _max;
    }

    /** 
     * Gets the name of the optional Selector associated to the Table (i&#46;e&#46;: Subscription).
     * 
     * @return  the name of the optional Selector associated to the Table
     */
    @Nullable
    public final String getSelector() {
        return _selector;
    }

    /**
     * Returns the array of the Item names involved in this Table (i&#46;e&#46;: Subscription).
     * The sequence of names is the same one returned by {@link MetadataProvider#getItems(String, String, String)}
     * when decoding of the group name, but restricted, in case a first and/or last
     * Item was specified in the client request (see {@link #getMin()} and {@link #getMax()}). 
     *
     * @return an Array of Item names.
     */
    @Nonnull
    public String[] getSubscribedItems() {
        return _itemNames;
    }

    /**
     * Returns an array that contains the statistics related with the activity
     * of all items involved in this Table (i&#46;e&#46;: Subscription).
     * Each entry refers to one item and the order is the same as returned
     * by {@link #getSubscribedItems()}.
     * <BR>These statistics are available only on the objects supplied by calls
     * to {@link MetadataProvider#notifyTablesClose}, 
     * so that the statistics will refer to the whole life of the subscription.
     * Moreover, the statistics are available only if previously enabled through
     * {@link MetadataProvider#wantsFinalTableStatistics(String, TableInfo[])}.
     *
     * @return an Array of statistics-gathering objects or null.
     */
    @Nullable
    public SubscriptionStatistics[] getSubscriptionStatistics() {
        return _statistics;
    }
    
}