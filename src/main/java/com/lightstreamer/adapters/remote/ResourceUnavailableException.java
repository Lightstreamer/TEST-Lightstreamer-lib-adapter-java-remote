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

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Can be thrown by the {@link MetadataProvider#notifyUser(String, String, Map)} methods
 * instead of a generic {@link AccessException} if the User credentials
 * cannot be validated because of a temporary lack of resources.
 * <BR>When receiving this exception, the Server will instruct the client
 * to retry in short time.
 */
public class ResourceUnavailableException extends AccessException {

    /**
     * Constructs a ResourceUnavailableException with a supplied error message text.
     *
     * @param msg The detail message.
     */
    public ResourceUnavailableException(@Nullable String msg) {
        super(msg);
    }
}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
