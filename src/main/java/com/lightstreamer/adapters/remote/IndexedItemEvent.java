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
 * Provides to the Data Adapter an alternative interface for creating Item Events
 * in order to send updates to Lightstreamer Kernel.
 * In this event, a name-index association is defined for all fields. These 
 * indexes will be used by the Remote Server to iterate through all the fields. Some indexes may not be associated to 
 * fields in the event, but the number of such holes should be small. The name-index associations are local 
 * to the event and may be different even across events belonging to the same Item.
 *
 * @deprecated The class is deprecated. Use a Map and
 * {@link ItemEventListener#update(String, java.util.Map, boolean)}
 * to supply field values. 
 */
@Deprecated
public interface IndexedItemEvent {

    /** 
     * Returns the maximum index for the fields in the event. The event cannot be empty, so the maximum 
     * Index must always exist.
     * 
     * @return A 0-based index.
     */
    int getMaximumIndex();

    /** 
     * Returns the index of a named Field. Returns -1 if such a field is not reported in this event. 
     * So, the implementation must be very fast.
     * 
     * @param name A Field name.
     * @return A 0-based index for the field or -1. The index must not be greater than the maximum index 
     * returned by getMaximumIndex().
     */
    int getIndex(@Nonnull String name);

    /** 
     * Returns the name of a Field whose index is supplied. Returns null if the Field is not reported in 
     * this event.
     * 
     * @param index A Field index.
     * @return The name of a Field, or null.
    */
    @Nullable
    String getName(int index);

    /** 
     * Returns the value of a field whose index is supplied (null is a legal value too). Returns null if 
     * the Field is not reported in the Item Event. The value should be expressed as a String;
     * the use of a byte array, to supply a string encoded in the ISO-8859-1 (ISO-LATIN-1)
     * character set, is also allowed, but it has been deprecated.
     * 
     * @param index A Field index.
     * @return A String containing the Field value, or null. A byte array is also accepted, but deprecated.
     */
    @Nullable
    Object getValue(int index);
}