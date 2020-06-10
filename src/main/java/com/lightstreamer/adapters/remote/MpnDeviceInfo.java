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
 * Specifies a target device for Push Notifications, used with MPN-related requests for the {@link MetadataProvider}.
 * <BR>Note that the processing and the authorization of Push Notifications is per-device and per-application.
 * While a physical device is uniquely identified by the platform type and a platform dependent device token,
 * Lightstreamer considers the same device used by two different applications as two different MPN devices. 
 * Thus, an MpnDeviceInfo instance uniquely identifies both the physical device and the application for which 
 * it is being used.
 * <BR>
 * <BR>An MpnDeviceInfo always provides the following identifiers:<UL>
 * <LI>The platform type.</LI>
 * <LI>The application ID.</LI>
 * <LI>The device token.</LI>
 * </UL>
 * <br>
 * <b>Edition Note:</b>
 * <br>Push Notifications is an optional feature,
 * available depending on Edition and License Type.
 * To know what features are enabled by your license, please see the License
 * tab of the Monitoring Dashboard (by default, available at /dashboard).
 */

public class MpnDeviceInfo {
    private MpnPlatformType _type;
    private String _applicationId;
    private String _deviceToken;

    /** 
     * Used by Lightstreamer to provide a MpnDeviceInfo instance to the MPN-related methods.
     * 
     * @param type Platform type of the device.
     * @param applicationId The app ID, also known as the bundle ID on some platforms.
     * @param deviceToken The token of the device.
     */
    public MpnDeviceInfo(@Nonnull MpnPlatformType type, @Nonnull String applicationId, @Nonnull String deviceToken) {

        _type = type;
        _applicationId = applicationId;
        _deviceToken = deviceToken;
    }

    /** 
     * Returns the platform type of the device.
     * @return a platform type.
     */
    @Nonnull
    public final MpnPlatformType getType() {
        return _type;
    }

    /** 
     * Returns the application ID, also known as the package name or bundle ID on some platforms.
     * @return an application ID
     */
    @Nonnull
    public final String getApplicationId() {
        return _applicationId;
    }

    /** 
     * Returns the token of the device, also know as the registration ID on some platforms.
     * @return a device token.
     */
    @Nonnull
    public final String getDeviceToken() {
        return _deviceToken;
    }
    
    /**
     * Returns a string representation of the MpnDeviceInfo.
     * An MpnDeviceInfo object is represented by a juxtaposition of its three properties 
     * platform type, application ID and device token, separated by a single "/" character.
     * E.g.:
     * <pre>
     * Apple/com.lightstreamer.ios.stocklist/8fac[...]fe12
     * </pre> 
     * 
     * @return a string representation of the MPN device info.
     */
    @Override @Nonnull 
    public String toString() {
        return _type.getName() + "/" +  _applicationId + "/" + _deviceToken;
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * Two MpnDeviceInfo objects are equal if their three properties are equal.
     * 
     * @return true if this object is equal to the obj argument; false otherwise.
     */
    @Override 
    public boolean equals(@Nullable Object obj) {
        MpnDeviceInfo deviceInfo = (MpnDeviceInfo)((obj instanceof MpnDeviceInfo) ? obj : null);
        if (deviceInfo == null) {
            return false;
        }

        return _type.equals(deviceInfo._type) && 
                _applicationId.equals(deviceInfo._applicationId) && 
                _deviceToken.equals(deviceInfo._deviceToken);
    }

    /**
     * Returns a hash code value for the object.
     * 
     * @return a hash code value for this object.
     */
    @Override 
    public int hashCode() {
        return _type.hashCode() ^ _applicationId.hashCode() ^ _deviceToken.hashCode();
    }
}