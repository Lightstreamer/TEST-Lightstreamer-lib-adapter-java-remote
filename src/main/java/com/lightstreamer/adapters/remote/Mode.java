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

/** 
 * Encapsulates a publishing Mode. The different Modes handled by Lightstreamer Kernel can be uniquely 
 * identified by the static constants defined in this class. See the technical documents for a detailed 
 * description of Modes.
*/
public class Mode {

    /**
     * The RAW Mode.
     */
	@Nonnull
    public static final Mode RAW = new Mode("RAW");

    /** 
     * The MERGE Mode.
     */
	@Nonnull
    public static final Mode MERGE = new Mode("MERGE");

    /**
     * The DISTINCT Mode.
     */
	@Nonnull
    public static final Mode DISTINCT = new Mode("DISTINCT");

    /** 
     * The COMMAND Mode.
     */
	@Nonnull
    public static final Mode COMMAND = new Mode("COMMAND");

    private String _name;

    private Mode(String name) {
        _name = name;
    }

    /** Method ToString.
     @return  ...
     
    */
    @Override @Nonnull
    public String toString() {
        return _name;
    }

    /** Method Equals.
     @param other ...
     
     @return  ...
     
    */
    @Override
    public boolean equals(@Nonnull Object other) {
        Mode mode = (Mode)((other instanceof Mode) ? other : null);
        if (mode == null) {
            return false;
        }

        return mode._name.equals(_name);
    }
}