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
 * Specifies a Push Notifications subscription, used with MPN-related requests of the {@link MetadataProvider}.
 * <BR>For the actual description of the subscription we rely on a generic descriptor accessible
 * via the {@link #getNotificationFormat()} getter, where the structure of the descriptor depends on the platform.
 * 
 * @general_edition_note Push Notifications is an optional feature,
 * available depending on Edition and License Type.
 * To know what features are enabled by your license, please see the License
 * tab of the Monitoring Dashboard (by default, available at /dashboard).
 */
public class MpnSubscriptionInfo {
    private MpnDeviceInfo _device;
    private String _notification;
    private String _trigger;

    /** 
     * Used by Lightstreamer to create a MpnSubscriptionInfo instance. 
     * 
     * @param device The MPN device of the push notifications.
     * @param notification the descriptor of the push notifications format.
     * @param trigger The expression the updates are checked against to trigger the notification.
     */
    public MpnSubscriptionInfo(@Nonnull MpnDeviceInfo device, @Nonnull String notification, @Nonnull String trigger) {
        _device = device;
        _notification= notification;
        _trigger = trigger;
    }

    /** 
     * Gets the MPN device of this subscription.
     * @return the MPN device of this subscription.
     */
    @Nonnull
    public final MpnDeviceInfo getDevice() {
        return _device;
    }

    /**
     * Returns the descriptor of the push notifications format of this subscription.
     * <BR>The structure of the format descriptor depends on the platform type
     * and it is represented in json.
     * 
     * @return a descriptor of the push notifications format in json.
     */
    @Nonnull
    public String getNotificationFormat() {
        return _notification;
    }

    /** 
     * Returns the optional expression that triggers the delivery of push notification.
     * @return a trigger expression, or null if not specified.
     */
    @Nonnull
    public final String getTrigger() {
        return _trigger;
    }

    /**
     * Returns a string representation of the MpnSubscriptionInfo.
     * An MpnSubscriptionInfo object is represented by its three properties
     * device, trigger and notification format, prefixed by their name and
     * on separate lines. E.g.:
     * <pre>
     * device=Apple/com.lightstreamer.ios.stocklist/8fac[...]fe12\n
     * trigger=Double.parseDouble(${last_price}) &gt;= 50.0\n
     * notificationFormat={aps={badge=AUTO, alert=Price is over 50$, sound=Default}}\n
     * </pre> 
     * 
     * @return a string representation of the MpnSubscriptionInfo.
     */
    @Override @Nonnull
    public String toString() {
        return "device=" + _device + System.getProperty("line.separator") + 
                "trigger=" + _trigger + System.getProperty("line.separator") + 
                "notificationFormat=" + _notification;
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * Two MpnSubscriptionInfo objects are equal if their three properties are equal.
     * 
     * @return true if this object is equal to the obj argument; false otherwise.
     */
    @Override 
    public boolean equals(@Nullable Object obj) {
        MpnSubscriptionInfo subscriptionInfo= (MpnSubscriptionInfo)((obj instanceof MpnSubscriptionInfo) ? obj : null);
        if (subscriptionInfo == null) {
            return false;
        }

        return _device.equals(subscriptionInfo._device) && 
                ((_trigger != null) ? _trigger.equals(subscriptionInfo._trigger) : (subscriptionInfo._trigger == null)) && 
                _notification.equals(subscriptionInfo._notification);
    }

    /**
     * Returns a hash code value for the object.
     * 
     * @return a hash code value for this object.
     */
    @Override 
    public int hashCode() {
        return _device.hashCode() ^ ((_trigger != null) ? _trigger.hashCode() : 0) ^ _notification.hashCode();
    }

}