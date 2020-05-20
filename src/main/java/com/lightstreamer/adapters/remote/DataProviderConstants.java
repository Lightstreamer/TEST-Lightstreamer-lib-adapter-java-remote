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

import javax.annotation.Nullable;

/** 
 * Contains constants for the special field names and field values recognized by the Server.
 */
public class DataProviderConstants {

    /** 
     * Constant that can be used as field name for the "key" field in Items to be processed in COMMAND mode.
     */
	@Nullable
    public static final String KEY_FIELD = "key";

    /** 
     * Constant that can be used as field name for the "command" field in Items to be processed in 
     * COMMAND mode.
     */
	@Nullable
    public static final String COMMAND_FIELD = "command";

    /** 
     * Constant that can be used as the "ADD" value for the "command" fields of Items to be processed in 
     * COMMAND mode.
     */
	@Nullable
    public static final String ADD_COMMAND = "ADD";

    /** 
     * Constant that can be used as the "UPDATE" value for the "command" fields of Items to be processed 
     * in COMMAND mode.
     */
	@Nullable
    public static final String UPDATE_COMMAND = "UPDATE";

    /** 
     * Constant that can be used as the "DELETE" value for the "command" fields of Items to be processed 
     * in COMMAND mode.
     */
	@Nullable
    public static final String DELETE_COMMAND = "DELETE";
}