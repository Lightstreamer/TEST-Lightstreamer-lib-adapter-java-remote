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
 * Identifies a Push Notifications platform type, used with MPN-related requests of the {@link MetadataProvider}.
 * <BR>It is used by Lightstreamer to specify the platform associated with the notified client requests.
 * <BR>The available constants refer to the platform types currently supported.
 * 
 * <br>
 * <b>Edition Note:</b>
 * <br>Push Notifications is an optional feature,
 * available depending on Edition and License Type.
 * To know what features are enabled by your license, please see the License
 * tab of the Monitoring Dashboard (by default, available at /dashboard).
 */
public class MpnPlatformType {
    private String _name;

    /** 
     * Used by Lightstreamer to create a MpnPlatformType instance. 
     * @param name A platform type name.
     */
    public MpnPlatformType(@Nonnull String name) {
        _name = name;
    }

    /** 
     * Returns the internal name of the platform type. 
     * @return the platform type internal name.
     */
    @Nonnull
    public final String getName() {
        return _name;
    }

    /** 
     * Refers to Push Notifications for Apple platforms, such as iOS, macOS and tvOS.
     * The back-end service for Apple platforms is APNs ("Apple Push Notification service").
     * Apple, iOS, macOS and tvOS are registered trademarks of Apple, Inc. 
     */
    @Nonnull
    public static final MpnPlatformType Apple = new MpnPlatformType("Apple");

    /** 
     * Refers to Push Notifications for Google platforms, such as Android and Chrome.
     * The back-end service for Google platforms is FCM ("Firebase Cloud Messaging").
     * Google, Android and Chrome are registered trademarks of Google Inc.
     */
    @Nonnull
    public static final MpnPlatformType Google = new MpnPlatformType("Google");

    /**
     * Returns a string representation of the MpnPlatformType.
     * An MpnPlatformType object is represented by its internal name.
     * E.g.:
     * <pre>
     * Apple
     * </pre>
     * 
     * @return a string representation of the MpnPlatformType.
     */
    @Override @Nonnull
    public String toString() {
        return _name;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two MpnPlatformType objects are equal if their internal names are equal.
     * 
     * @return true if this object is equal to the obj argument; false otherwise.
     */
    @Override 
    public boolean equals(@Nullable Object obj) {
        MpnPlatformType platformType = (MpnPlatformType)((obj instanceof MpnPlatformType) ? obj : null);
        if (platformType == null) {
            return false;
        }

        return platformType._name.equals(_name);
    }

    /**
     * Returns a hash code value for the object.
     * 
     * @return a hash code value for this object.
     */
    @Override 
    public int hashCode() {
        return _name.hashCode();
    }
}