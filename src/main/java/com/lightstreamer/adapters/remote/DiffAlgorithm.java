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

/**
 * Enumerative class that lists the available "diff" algorithms
 * that can be specified in {@link ItemEventListener#declareFieldDiffOrder}.
 * Such algorithms can be used by the Server to compute the difference
 * between a value and the previous one in order to send the client this
 * difference, for "delta delivery" purpose.
 * <BR>More algorithms may be added to the enumerative class
 * as their support is added to the Server.
 */
public enum DiffAlgorithm {
    
    /**
     * Computes the difference between two values that are valid JSON
     * string representations in JSON Patch format.
     * <BR>Note that this setting is ignored for the mandatory fields
     * for COMMAND Mode named "key" and "command".
     */
    JSONPATCH,
    
    /**
     * Computes the difference between two values with Google's
     * "diff-match-patch" algorithm (the result is then serialized with
     * the custom "TLCP-diff" format). This algorithm applies to any strings,
     * only provided that they don't contain UTF-16 surrogate pairs.
     */
    DIFF_MATCH_PATCH
}


