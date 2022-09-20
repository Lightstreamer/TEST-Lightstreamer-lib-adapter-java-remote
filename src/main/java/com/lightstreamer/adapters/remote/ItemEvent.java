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
 * An ItemEvent object contains the new values and, in some cases, the current 
 * values of the Fields of an Item. All implementation methods should be nonblocking.
 *
 * @deprecated The class is deprecated. Use a Map and
 * {@link ItemEventListener#update(String, java.util.Map, boolean)}
 * to supply field values. 
 */
@Deprecated
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
     * reported in the Item Event. The value should be expressed as a String;
     * the use of a byte array, to supply a string encoded in the ISO-8859-1 (ISO-LATIN-1)
     * character set, is also allowed, but it has been deprecated.
     * The Remote Server, will call this method only once for each Field.
     * 
     * @param name A Field name.
     * @return A String containing the Field value, or null. A byte array is also accepted, but deprecated.
     */
	@Nullable
    Object getValue(@Nonnull String name);
}