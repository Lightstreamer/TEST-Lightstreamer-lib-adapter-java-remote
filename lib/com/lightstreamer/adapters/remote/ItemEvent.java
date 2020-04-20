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

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/** 
 * Provides to the Data Adapter a base interface for creating Item Events
 * in order to send updates to Lightstreamer Kernel.
 * An Item Event object contains the new values and, in some cases, the current 
 * values of the Fields of an Item. The interfaces IndexedItemEvent and java.util.Map may 
 * also be used to define events. Events of all these kinds may be freely mixed, even if they belong to the 
 * same Item. All implementation methods should be nonblocking.
 */
public interface ItemEvent {

    /** 
     * Returns an Iterator to browse the names of the supplied Fields, expressed as String.
     * 
     * @return an Iterator to browse the names of the supplied Fields.
     */
	@Nonnull
    Iterator<String> getNames();

    /** 
     * Returns the value of a named Field (null is a legal value too). Returns null if the Field is not 
     * reported in the Item Event. The value can be expressed as either a String or a byte array, the latter 
     * case being the most efficient, though restricted to the ISO-8859-1 (ISO-LATIN-1) character set. 
     * Lightstreamer Kernel, through the Remote Server, will call this method
     * at most once for each Field (unless events logging is 
     * enabled) and may not call this method at all for some Fields. So, if performing any data conversion is 
     * required in order to extract Field values, it may be convenient to do it on demand rather than doing 
     * it in advance.
     * 
     * @param name A Field name.
     * @return A String or a byte array containing the Field value, or null.
     */
	@Nullable
    Object getValue(@Nonnull String name);
}