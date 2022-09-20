/*
 *  Copyright (c) Lightstreamer Srl
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
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
 * Used by Lightstreamer to provide to method {@link TableInfo#getSubscriptionStatistics()}
 * value objects to report activity statistics for single subscribed items involved
 * in a Table (i&#46;e&#46;: Subscription).
 */
public final class SubscriptionStatistics {
    private final long totRealTime;
    private final long totLost;
    private final long totFiltered;
    
    /**
     * Used by Lightstreamer to create a SubscriptionStatistics instance,
     * collecting various activity statistics for a single Item.
     * 
     * @param totRealTime the number of real-time updates sent to the client.
     * Events that are part of the initial snapshot are not included.
     * @param totLost the number of updates that had to be sent the client,
     * but weren't, because of buffer restrictions.
     * @param totFiltered the number of updates that were not sent to the
     * client, but filtered out according to the filtering rules.
     */
    public SubscriptionStatistics(long totRealTime, long totLost, long totFiltered) {
        this.totRealTime = totRealTime;
        this.totLost = totLost;
        this.totFiltered = totFiltered;
    }

    /**
     * Returns the number of real-time updates sent to the client
     * throughout the life of a subscription.
     * Events that are part of the initial snapshot are not included.
     * 
     * @return The number of updates sent.
     */
    public long getTotRealTime() {
        return totRealTime;
    }

    /**
     * Returns the number of updates that had to be sent the client,
     * but weren't, throughout the life of a subscription.
     * This is only possible in case of buffer restrictions posed for safety purpose.
     * <BR>This applies to Items subscribed to in RAW mode,
     * or in any mode with unfiltered dispatching specified.
     * This also applies to items subscribed to in COMMAND mode
     * with filtered dispatching, restricted to "ADD" and "DELETE" events only
     * (note that those events can also be filtered through matching).
     * Any lost event is also notified to the client.
     * 
     * @return The number of updates lost.
     */
    public long getTotLost() {
        return totLost;
    }

    /**
     * Returns the number of updates that were not sent to the client, but filtered out
     * according to the filtering rules, throughout the life of a subscription.
     * <BR>Events can be filtered only if this is allowed by the item
     * subscription mode; in that case, no information on the current
     * state of the item is lost and the client is not notified.
     * Events lost as specified for {@link #getTotLost()} are not
     * considered as filtered.
     * Events discarded because of the application of a "selector" are
     * also included in this count, regardless of the subscription mode.
     * 
     * @return The number of updates filtered out.
     */
    public long getTotFiltered() {
        return totFiltered;
    }

}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
