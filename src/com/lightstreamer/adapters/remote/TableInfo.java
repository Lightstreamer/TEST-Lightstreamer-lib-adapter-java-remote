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
 * to methods NotifyNewTables and NotifyTablesClose.
 * The attributes of every Table (i.e.: Subscription) to be added or removed
 * to a Session have to be written to a TableInfo instance.
 */
public class TableInfo {
    private int _winIndex;
    private Mode _mode;
    private String _group;
    private String _schema;
    private int _min;
    private int _max;
    private String _selector;

    /** 
     * Used by Lightstreamer to create a TableInfo instance,
     * collecting the various attributes of a Table (i.e.: Subscription).
     * 
     * @param winIndex Unique identifier of the client subscription request within the session.
     * @param mode Publishing Mode for the Items in the Table (i.e. Subscription)
     * (it must be the same across all the Table).
     * @param group The name of the Item Group (or specification of the Item List)
     * to which the subscribed Items belong.
     * @param schema The name of the Field Schema (or specification of the Field List)
     * used for the subscribed Items.
     * @param min The 1-based index of the first Item in the Group to be considered in the 
     * Table (i.e. Subscription).
     * @param max The 1-based index of the last Item in the Group to be considered in the 
     * Table (i.e. Subscription).
     * @param selector The name of the optional Selector associated to the table (i.e. Subscription).
    */
    public TableInfo(int winIndex, @Nonnull Mode mode, @Nonnull String group, @Nonnull String schema, int min, int max, @Nullable String selector) {
        _winIndex = winIndex;
        _mode = mode;
        _group = group;
        _schema = schema;
        _min = min;
        _max = max;
        _selector = selector;
    }

    /** 
     * Gets the unique identifier of the client subscription request within the session.
     * This allows for matching the corresponding subscription and unsubscription requests.
     * Note that, for clients based on a very old version of a client library
     * or text protocol, subscription requests may involve multiple Tables
     * (i.e.: Subscriptions), hence multiple objects of this type can be supplied
     * in a single array by MetadataProvider through NotifyNewTables and
     * NotifyTablesClose. In this case, the value returned
     * is the same for all these objects and the single Tables (i.e.: Subscriptions)
     * can be identified by their relative position in the array.
     * 
     * @return the unique identifier of the client subscription request.
     */
    public final int getWinIndex() {
        return _winIndex;
    }

    /** 
     * Gets the publishing Mode for the Items in the Table (i.e. Subscription)
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
     * to be considered in the Table (i.e. Subscription).
     * 
     * @return the index of the first Item in the Group
     */
    public final int getMin() {
        return _min;
    }

    /** 
     * Gets the index of the last Item in the Group
     * to be considered in the Table (i.e. Subscription).
     * 
     * @return  the index of the last Item in the Group
     */
    public final int getMax() {
        return _max;
    }

    /** 
     * Gets the name of the optional Selector associated to the Table (i.e. Subscription).
     * 
     * @return  the name of the optional Selector associated to the Table
     */
    @Nullable
    public final String getSelector() {
        return _selector;
    }
}