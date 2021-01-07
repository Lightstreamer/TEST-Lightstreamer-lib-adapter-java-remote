/*
 * Copyright (c) Lightstreamer Srl
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lightstreamer.adapters.remote.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.lightstreamer.adapters.remote.AccessException;
import com.lightstreamer.adapters.remote.CreditsException;
import com.lightstreamer.adapters.remote.ItemsException;
import com.lightstreamer.adapters.remote.MetadataProviderAdapter;
import com.lightstreamer.adapters.remote.MetadataProviderException;
import com.lightstreamer.adapters.remote.SchemaException;

/**
 * Simple full implementation of a Metadata Adapter, made available
 * in Lightstreamer SDK. <BR>
 * 
 * The class handles Item List specifications, a special case of Item Group name
 * formed by simply concatenating the names of the Items contained in a List
 * in a space separated way. Similarly, the class
 * handles Field List specifications, a special case of Field Schema name
 * formed by concatenating the names of the contained Fields. <BR>
 * 
 * The resource levels are assigned the same for all Items and Users,
 * according with values that can be supplied together with adapter
 * configuration, inside the "metadata_provider" element that defines the
 * Adapter. <BR>
 * The return of the getAllowedMaxBandwidth method can be supplied in a
 * "max_bandwidth" parameter; the return of the getAllowedMaxItemFrequency
 * method can be supplied in a "max_frequency" parameter; the return of the
 * getAllowedBufferSize method can be supplied in a "buffer_size" parameter;
 * the return of the getDistinctSnapshotLength method can be supplied
 * in a "distinct_snapshot_length" parameter. All resource limits not supplied
 * are granted as unlimited, but for distinct_snapshot_length, which defaults as 10. <BR>
 * There are no access restrictions, but an optional User name check is
 * performed if a comma separated list of User names is supplied in an
 * "allowed_users" parameter.
 */
public class LiteralBasedProvider extends MetadataProviderAdapter {

    private String [] _allowedUsers;
    private double _maxBandwidth;
    private double _maxFrequency;
    private int _bufferSize;
    private int _distinctSnapshotLength;

    /** 
     * Void constructor required by the Remote Server.
     */
    public LiteralBasedProvider() {
    }

    /**
     * Reads configuration settings for user and resource constraints.
     * If some setting is missing, the corresponding constraint is not set.
     *
     * @param  parameters  Can contain the configuration settings. 
     * @param configFile Not used.
     * @throws MetadataProviderException in case of configuration errors.
     */
    @Override
    public void init(Map<String,String> parameters, String configFile) throws MetadataProviderException {
        if (parameters == null) {
            parameters = new HashMap<String,String>();
        }

        String users = parameters.get("allowed_users");
        if (users != null) {
            _allowedUsers = mySplit(users, ",");
        }

        try {
            String mb = parameters.get("max_bandwidth");
            if (mb != null) {
                _maxBandwidth = Double.parseDouble(mb);
            }
    
            String mf = parameters.get("max_frequency");
            if (mf != null) {
                _maxFrequency = Double.parseDouble(mf);
            }
    
            String bs = parameters.get("buffer_size");
            if (bs != null) {
                _bufferSize = Integer.parseInt(bs);
            }
    
            String dsl = parameters.get("distinct_snapshot_length");
            if (dsl != null) {
                _distinctSnapshotLength = Integer.parseInt(dsl);
            } else {
                _distinctSnapshotLength = 10;
            }
        } catch(NumberFormatException nfe) {
            throw new MetadataProviderException("Configuration error: " + nfe.getMessage());
        }
    }

    /**
     * Resolves an Item List specification supplied in a Request. The names of the Items
     * in the List are returned.
     * <BR>Item List Specifications are expected to be formed by simply concatenating the names
     * of the contained Items, in a space separated way.
     *
     * @param user A User name.
     * @param sessionID A Session ID. Not used.
     * @param itemList An Item List specification.
     * @return An array with the names of the Items in the List.
     */
    @Override
    public String[] getItems(String user, String sessionID, String itemList) {
        return mySplit(itemList, " ");
    }

    /**
     * Resolves a Field List specification supplied in a Request. The names of the Fields
     * in the List are returned.
     * <BR>Field List specifications are expected to be formed by simply concatenating the names
     * of the contained Fields, in a space separated way.
     *
     * @param user A User name. Not used.
     * @param sessionID A Session ID. Not used.
     * @param itemList The specification of the Item List whose Items the Field List
     * is to be applied to.
     * @param fieldList A Field List specification.
     * @return An array with the names of the Fields in the List.
     */
    @Override
    public String[] getSchema(String user, String sessionID, String itemList, String fieldList) {
        return mySplit(fieldList, " ");
    }

    /**
     * Checks if a user is enabled to make Requests to the related Data
     * Providers.
     * If a list of User names has been configured, this list is checked.
     * Otherwise, any User name is allowed. No password check is performed.
     *
     * @param user A User name.
     * @param password An optional password. Not used.
     * @param httpHeaders A Map that contains a name-value pair for each
     * header found in the HTTP request that originated the call. Not used.
     * @throws AccessException if a list of User names has been configured
     * and the supplied name does not belong to the list.
     */
    @Override
    public void notifyUser(String user, String password, Map<String,String> httpHeaders) throws AccessException {
        if (!checkUser(user)) {
            throw new AccessException("Unauthorized user");
        }
    }
    
    /**
     * Returns the bandwidth level to be allowed to a User for a push Session.
     *
     * @param user A User name. Not used.
     * @return The bandwidth, in Kbit/sec, as supplied in the Metadata
     * Adapter configuration.
     */
    @Override
    public double getAllowedMaxBandwidth(String user) {
        return _maxBandwidth;
    }

    /**
     * Returns the ItemUpdate frequency to be allowed to a User for a specific
     * Item.
     *
     * @param user A User name. Not used.
     * @param item An Item Name. Not used.
     * @return The allowed Update frequency, in Updates/sec, as supplied
     * in the Metadata Adapter configuration.
     */
    @Override
    public double getAllowedMaxItemFrequency(String user, String item) {
        return _maxFrequency;
    }

    /**
     * Returns the size of the buffer internally used to enqueue subsequent
     * ItemUpdates for the same Item.
     *
     * @param user A User name. Not used.
     * @param item An Item Name. Not used.
     * @return The allowed buffer size, as supplied in the Metadata Adapter
     * configuration.
     */
    @Override
    public int getAllowedBufferSize(String user, String item) {
        return _bufferSize;
    }

    /**
     * Returns the maximum allowed length for a Snapshot of any Item that
     * has been requested with publishing Mode DISTINCT.
     *
     * @param item An Item Name.
     * @return The maximum allowed length for the Snapshot, as supplied
     * in the Metadata Adapter configuration. In case no value has been
     * supplied, a default value of 10 events is returned, which is thought
     * to be enough to satisfy typical Client requests.
     */
    @Override
    public int getDistinctSnapshotLength(String item) {
        return _distinctSnapshotLength;
    }

    // ////////////////////////////////////////////////////////////////////////
    // Internal methods

    private boolean checkUser(String user) {
        if ((_allowedUsers == null) || (_allowedUsers.length == 0)) {
            return true;
        }
        if (user == null) {
            return false;
        }
        for (int i = 0; i < _allowedUsers.length; i++) {
            if (_allowedUsers[i] == null) {
                continue;
            }
            if (_allowedUsers[i].equals(user)) {
                return true;
            }
        }
        return false;
    }

    private String[] mySplit(String arg, String separator) {
        if (arg.indexOf(separator) < 0) {
            String[] results = new String[1];
            results[0] = arg;
            return results;
        }
        
        StringTokenizer tokenizer = new StringTokenizer(arg, separator);
        String[] results = new String[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            results[i] = tokenizer.nextToken();
        }
        return results;
    }
}