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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides an interface to be implemented by a Remote Metadata Adapter in order
 * to attach a Metadata Provider to Lightstreamer.
 * An instance of a Remote Metadata Adapter is supplied to Lightstreamer
 * through a {@link MetadataProviderServer} instance.
 * <BR>
 * A Metadata Provider is used by Lightstreamer Kernel
 * in combination with one or multiple Data Providers, uniquely associated with it; it is consulted
 * in order to manage the push Requests intended for the associated Data Providers.
 * A Metadata Provider supplies information for several different goals:
 * <UL>
 * <LI> the resolution of the Group/Schema names used in the Requests; </LI>
 * <LI> the check of the User accessibility to the requested Items; </LI>
 * <LI> the check of the resource level granted to the User. </LI>
 * <LI> the request for specific characteristics of the Items. </LI>
 * </UL>
 * Note: Each Item may be supplied by one or more of the associated Data
 * Adapters and each client Request must reference to a specific Data Adapter.
 * However, in the current version of the interface, no Data Adapter
 * information is supplied to the Metadata Adapter methods. Hence, the Item
 * names must provide enough information for the methods to give an answer.
 * As a consequence, for instance, the frequency, snapshot length and other
 * characteristics of an item are the same regardless of the Data Adapter
 * it is requested from. More likely, for each item name defined, only one
 * of the Data Adapters in the set is responsible for supplying that item.
 * <BR>
 * All implementation methods should perform as fast as possible.
 * See the notes on the corresponding methods in the Java In-Process interface
 * for the method-related details. Also consider that the roundtrip time
 * involved in the remote call adds up to each call time anyway.
 * <BR>
 * In order to avoid that delays on calls for one session
 * propagate to other sessions, the size of the thread pool devoted to the
 * management of the client requests should be properly set, through the
 * &lt;server_pool_max_size&gt; flag, in the Server configuration file.
 * <BR>
 * Alternatively, a dedicated pool, properly sized, can be defined
 * for the involved Adapter Set in the adapters.xml. Still more restricted
 * dedicated pools can be defined for the authorization-related calls
 * and for each Data Adapter in the Adapter Set. The latter pool would also
 * run any Metadata Adapter method related to the items supplied by the
 * specified Data Adapter.
 */
public interface MetadataProvider {

    /**
     * Called by Lightstreamer Kernel through the Remote Server to provide initialization information
     * to the Metadata Adapter.
     * <BR>
     * <BR>
     * The call must not be blocking; any polling cycle or similar must be
     * started in a different thread. Any delay in returning from this call
     * will in turn delay the Server initialization.
     * If an exception occurs in this method, Lightstreamer Kernel can't
     * complete the startup and must exit.
     *
     * @param parameters A Map-type value object that contains name-value pairs
     * corresponding to the parameters elements supplied for the Metadata Adapter configuration.
     * Both names and values are represented as String objects. <BR>
     * The parameters can be supplied through the {@link MetadataProviderServer#setAdapterParams}
     * method of the MetadataProviderServer instance.
     * More parameters can be added by leveraging the "init_remote" parameter
     * in the Proxy Adapter configuration.
     * @param configFile  The path on the local disk of the Metadata Adapter configuration file.
     * Can be null if not specified.
     * The file path can be supplied through the {@link MetadataProviderServer#setAdapterConfig}
     * method of the used MetadataProviderServer instance.
     * @throws MetadataProviderException in case an error occurs that prevents the correct behavior of the Metadata Adapter.
     */
    void init(@Nonnull Map<String,String> parameters, @Nullable String configFile) throws MetadataProviderException;
    
    /**
     * Called by the Remote Server to provide a listener to receive
     * requests about sessions and any asynchronous severe error notification.
     * If these features are not needed, the method can be left unimplemented
     * (sticking to its default implementation).
     * The listener is set after init and before any other method is called
     * and it is never changed.
     *
     * @param listener a listener.
     */
    default void setListener(@Nonnull MetadataControlListener listener) {
        // we will do without the listener
    }

    /**
     * Called by Lightstreamer Kernel through the Remote Server
     * as a preliminary check that a user is
     * enabled to make Requests to the related Data Providers.
     * It is invoked upon each session request and it is called prior to any
     * other session-related request. So, any other method with a User
     * argument can assume that the supplied User argument has already been
     * checked.
     * <BR>The User authentication should be based on the user and password
     * arguments supplied by the client. The full report of the request HTTP
     * headers is also available; they could be used in order to gather
     * information about the client, but should not be used for authentication,
     * as they may not be under full control by client code. See also the
     * discussion about the &lt;use_protected_js&gt; Server configuration
     * element, if available.
     * <BR>
     * <BR>This method runs in the Server authentication thread pool, if defined.
     *
     * @param user A User name.
     * @param password A password optionally required to validate the User.
     * @param httpHeaders A Map-type value object that contains a name-value
     * pair for each header found in the HTTP request that originated the call.
     * The header names are reported in lower-case form. <BR>
     * For headers defined multiple times, a unique name-value pair is reported,
     * where the value is a concatenation of all the supplied header values,
     * separated by a ",".
     * One pair is added by Lightstreamer Server; the name is "REQUEST_ID"
     * and the value is a unique id assigned to the client request.
     * @throws AccessException if the User name is not known or the supplied
     * password is not correct.
     * <BR>If the User credentials cannot be validated because of a temporary
     * lack of resources, then a {@link ResourceUnavailableException} can be
     * thrown. This will instruct the client to retry in short time.
     * @throws CreditsException if the User is known but is not enabled to
     * make further Requests at the moment.
     * 
     * @see #notifyUser(String, String, Map, String)
     */
    void notifyUser(@Nullable String user, @Nullable String password, @Nonnull Map<String,String> httpHeaders) throws AccessException, CreditsException;

    /**
     * Called by Lightstreamer Kernel, through the Remote Server,
     * instead of calling the 3-arguments version, in case the Server
     * has been instructed to acquire the client principal from the client TLS/SSL
     * certificate through the &lt;use_client_auth&gt; configuration flag. <BR>
     * Note that the above flag can be set for each listening port
     * independently (and it can be set for TLS/SSL ports only), hence, both
     * overloads may be invoked, depending on the port used by the client.
     * Also note that in case client certificate authentication is not
     * forced on a listening port through &lt;force_client_auth&gt;, a client
     * request issued on that port may not be authenticated, hence it may
     * have no principal associated. In that case, if &lt;use_client_auth&gt;
     * is set, this overload will still be invoked, with null principal. <BR>
     * See the base 3-arguments version for other notes.
     * 
     * <br>
     * <b>Edition Note:</b>
     * <br>https connections is an optional feature, available
     * depending on Edition and License Type.
	 * To know what features are enabled by your license, please see the License
	 * tab of the Monitoring Dashboard (by default, available at /dashboard).
     *
     * @param user A User name.
     * @param password A password optionally required to validate the User.
     * @param httpHeaders A Map-type value object that contains a name-value
     * pair for each header found in the HTTP request that originated the call.
     * @param clientPrincipal the identification name reported in the client
     * TLS/SSL certificate supplied on the socket connection used to issue the
     * request that originated the call; it can be null if client has not
     * authenticated itself or the authentication has failed.
     * @throws AccessException if the User name is not known or the supplied
     * password is not correct.
     * <BR>If the User credentials cannot be validated because of a temporary
     * lack of resources, then a {@link ResourceUnavailableException} can be
     * thrown. This will instruct the client to retry in short time.
     * @throws CreditsException if the User is known but is not enabled to
     * make further Requests at the moment.
     * 
     * @see #notifyUser(String, String, Map, String)
     */
    void notifyUser(@Nullable String user, @Nullable String password, @Nonnull Map<String,String> httpHeaders, @Nullable String clientPrincipal) throws AccessException, CreditsException;

    /**
     * Called by Lightstreamer Kernel through the Remote Server to resolve an 
     * Item Group name (or Item List specification) supplied in
     * a Request. The names of the Items in the Group must be returned.
     * For instance, the client could be allowed to specify the "NASDAQ100"
     * Group name and, upon that, the list of all items corresponding to the
     * stocks included in that index could be returned.
     * <BR>Possibly, the content of an Item Group may be dependent on the User
     * who is issuing the Request or on the specific Session instance.
     * <BR>
     * <BR>When an Item List specification is supplied, it is made of a space-separated
     * list of the names of the items in the List. This convention is used
     * by some of the subscription methods provided by the various client
     * libraries. The specifications for these methods require that
     * "A LiteralBasedProvider or equivalent Metadata Adapter is needed
     * on the Server in order to understand the Request".
     * <BR>When any of these interface methods is used by client code accessing
     * this Metadata Adapter, the supplied "group" argument should be inspected
     * as a space-separated list of Item names and an array with these names
     * in the same order should be returned.
     * <BR>
     * <BR>Another typical case is when the same Item has different contents
     * depending on the User that is issuing the request. On the Data Adapter
     * side, different Items (one for each User) can be used; nevertheless, on
     * the client side, the same name can be specified in the subscription
     * request and the actual user-related name can be determined and returned
     * here. For instance:
     * <pre>
     * {@code
     * if (group.equals("portfolio")) {
     *     String itemName = "PF_" + user;
     *     return new String[] { itemName };
     * } else if (group.startsWith("PF_")) {
     *     // protection from unauthorized use of user-specific items
     *     throw new ItemsException("Unexpected group name");
     * }
     * }
     * </pre>
     * Obviously, the two above techniques can be combined, hence any
     * element of an Item List can be replaced with a decorated or alternative
     * Item name: the related updates will be associated to the original name
     * used in the supplied Item List specification by client library code.
     * <BR>
     * This method runs in the Server thread pool specific for the Data Adapter 
     * that supplies the involved Items, if defined.
     *
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param group An Item Group name (or Item List specification).
     * @return An array with the names of the Items in the Group.
     * @throws ItemsException if the supplied Item Group name (or Item List specification) is not recognized.
     */
    @Nonnull
    String[] getItems(@Nullable String user, @Nonnull String sessionID, @Nonnull String group) throws ItemsException;

    /**
     * Called by Lightstreamer Kernel through the Remote Server to resolve a Field
     * Schema name (or Field List specification) supplied in
     * a Request. The names of the Fields in the Schema must be returned.
     * <BR>Possibly, the content of a Field Schema may be dependent on the User
     * who is issuing the Request, on the specific Session instance or on the
     * Item Group (or Item List) to which the Request is related.
     * <BR>
     * <BR>When a Field List specification is supplied, it is made of a space-separated
     * list of the names of the Fields in the Schema. This convention is used
     * by some of the subscription methods provided by the various client
     * libraries. The specifications for these methods require that
     * "A LiteralBasedProvider or equivalent Metadata Adapter is needed
     * on the Server in order to understand the Request".
     * <BR>When any of these interface methods is used by client code accessing
     * this Metadata Adapter, the supplied "schema" argument should be inspected
     * as a space-separated list of Field names and an array with these names
     * in the same order should be returned;
     * returning decorated or alternative Field names is also possible:
     * they will be associated to the corresponding names used in the
     * supplied Field List specification by client library code.
     * <BR>
     * This method runs in the Server thread pool specific
     * for the Data Adapter that supplies the involved Items, if defined.
     *
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param group The name of the Item Group (or specification of the Item List)
     * whose Items the Schema is to be applied to.
     * @param schema A Field Schema name (or Field List specification).
     * @return An array with the names of the Fields in the Schema.
     * @throws ItemsException if the supplied Item Group name (or Item List specification) is not recognized.
     * @throws SchemaException if the supplied Field Schema name (or Field List specification) is not recognized.
     */
    @Nonnull 
    String[] getSchema(@Nullable String user, @Nonnull String sessionID, @Nonnull String group, @Nonnull String schema) throws ItemsException, SchemaException;

    /**
     * Called by Lightstreamer Kernel  through the Remote Server  to ask for the bandwidth level to be
     * allowed to a User for a push Session.
     * <BR>
     * This method runs in the Server authentication thread pool, if defined.
     * 
     * <br>
     * <b>Edition Note:</b>
     * <br>Bandwidth Control is an optional feature, available depending
     * on Edition and License Type.
	 * To know what features are enabled by your license, please see the License
	 * tab of the Monitoring Dashboard (by default, available at /dashboard).
     *
     * @param user A User name.
     * @return The allowed bandwidth, in Kbit/sec. A zero return value means
     * an unlimited bandwidth.
     */
    double getAllowedMaxBandwidth(@Nullable String user);

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to ask
     * for the ItemUpdate frequency to be allowed to a User for a 
     * specific Item. An unlimited frequency can also be specified. Such filtering applies only to Items 
     * requested with publishing Mode MERGE, DISTINCT and COMMAND (in the latter case, the frequency 
     * limitation applies to the UPDATE events for each single key). If an Item is requested with publishing 
     * Mode MERGE, DISTINCT or COMMAND and unfiltered dispatching has been specified, then returning any 
     * limited maximum frequency will cause the refusal of the request by Lightstreamer Kernel. <BR>
     * This method runs in the Server thread pool specific
     * for the Data Adapter that supplies the involved items, if defined.
     * 
     * <br>
     * <b>Edition Note:</b>
     * <br> A further global frequency limit could also be imposed by the Server,
     * depending on Edition and License Type; this specific limit also applies to RAW mode and
     * to unfiltered dispatching.
	 * To know what features are enabled by your license, please see the License
	 * tab of the Monitoring Dashboard (by default, available at /dashboard).
     *
     * @param user A User name.
     * @param item An Item Name.
     * @return The allowed Update frequency, in Updates/sec. A zero return value means no frequency 
     * restriction.
     */
    double getAllowedMaxItemFrequency(@Nullable String user, @Nonnull String item);

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to ask
     * for the maximum size allowed for the buffer internally used to 
     * enqueue subsequent ItemUpdates for the same Item. If this buffer is more than 1 element deep, a short 
     * burst of ItemEvents from the Data Adapter can be forwarded to the Client without losses, though with 
     * some delay. The buffer size is specified in the Request. Its maximum allowed size can be different for 
     * different Users. Such buffering applies only to Items requested with publishing Mode MERGE or DISTINCT. 
     * However, if the Item has been requested with unfiltered dispatching, then the buffer size is always 
     * unlimited and buffer size settings are ignored. <BR>
     * This method runs in the Server thread pool specific
     * for the Data Adapter that supplies the involved items, if defined.
     *
     * @param user A User name.
     * @param item An Item Name.
     * @return The allowed buffer size. A zero return value means a potentially unlimited buffer.
     */
    int getAllowedBufferSize(@Nullable String user, @Nonnull String item);

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to ask
     * for the allowance of a publishing Mode for an Item. 
     * A publishing Mode can or cannot be allowed depending on the User. The Metadata Adapter should 
     * ensure that conflicting Modes are not both allowed for the same Item (even for different Users), 
     * otherwise some Requests will be eventually refused by Lightstreamer Kernel. The conflicting Modes are 
     * MERGE, DISTINCT and COMMAND. <BR>
     * This method runs in the Server thread pool specific
     * for the Data Adapter that supplies the involved items, if defined.
     *
     * @param user A User name.
     * @param item An Item Name.
     * @param mode A publishing Mode.
     * @return True if the publishing Mode is allowed.
     */
    boolean isModeAllowed(@Nullable String user, @Nonnull String item, @Nonnull Mode mode);

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to ask
     * for the allowance of a publishing Mode for an Item (for at 
     * least one User). The Metadata Adapter should ensure that conflicting Modes are not both allowed for 
     * the same Item. The conflicting Modes are MERGE, DISTINCT and COMMAND. <BR>
     * This method runs in the Server thread pool specific
     * for the Data Adapter that supplies the involved items, if defined.
     *
     * @param item An Item Name.
     * @param mode A publishing Mode.
     * @return True if the publishing Mode is allowed.
     */
    boolean modeMayBeAllowed(@Nonnull String item, @Nonnull Mode mode);

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to ask
     * for the minimum ItemEvent frequency from the Data Adapter at 
     * which the events for an Item are guaranteed to be delivered to the Clients without loss of information. 
     * In case of an incoming ItemEvent frequency greater than this value, Lightstreamer Kernel may prefilter 
     * the events. Such prefiltering applies only for Items requested with publishing Mode MERGE or DISTINCT. 
     * The frequency set should be greater than the ItemUpdate frequencies allowed to the different Users for 
     * that Item. Moreover, because this filtering is made without buffers, the frequency set should be far 
     * greater than the ItemUpdate frequencies allowed for that Item for which buffering of event bursts is 
     * desired. If an Item is requested with publishing Mode MERGE or DISTINCT and unfiltered dispatching, 
     * then specifying any limited source frequency will cause the refusal of the request by Lightstreamer Kernel. 
     * This feature is just for ItemEventBuffers protection against Items with a very fast flow on the Data 
     * Adapter and a very slow flow allowed to the Clients. If this is the case, but just a few Clients need 
     * a fast or unfiltered flow for the same MERGE or DISTINCT Item, the use of two differently named Items 
     * that receive the same flow from the Data Adapter is suggested. <BR>
     * This method runs in the Server thread pool specific
     * for the Data Adapter that supplies the involved items, if defined.
     *
     * @param item An Item Name.
     * @return The minimum ItemEvent frequency that must be processed without loss of information, in 
     * ItemEvents/sec. A zero return value indicates that incoming ItemEvents must not be prefiltered. If the 
     * ItemEvents frequency for the Item is known to be very low, returning zero allows Lightstreamer Kernel 
     * to save any prefiltering effort.
     */
    double getMinSourceFrequency(@Nonnull String item);

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to ask
     * for the maximum allowed length for a Snapshot of an Item that 
     * has been requested with publishing Mode DISTINCT. In fact, in DISTINCT publishing Mode, the Snapshot 
     * for an Item is made by the last events received for the Item and the Client can specify how many events
     * it would like to receive. Thus, Lightstreamer Kernel must always keep a buffer with some of the last 
     * events received for the Item and the length of the buffer is limited by the value returned by this 
     * method. The maximum Snapshot size cannot be unlimited. <BR>
     * This method runs in the Server thread pool specific
     * for the Data Adapter that supplies the involved items, if defined.
     *
     * @param item An Item Name.
     * @return The maximum allowed length for the Snapshot; a zero return value means that no Snapshot 
     * information should be kept.
     */
    int getDistinctSnapshotLength(@Nonnull String item);

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to forward
     * a message received by a User. The interpretation of the 
     * message is up to the Metadata Adapter. A message can also be refused. <BR>
     * This method runs in the Server thread pool specific
     * for the Adapter Set, if defined.
     * 
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param message A non-null string.
     * @exception CreditsException
     * in case the User is not enabled to send
     * the message or the message cannot be correctly managed.
     * @exception NotificationException
     * in case something is wrong in the
     * parameters, such as a nonexistent Session ID.
     * 
     */
    void notifyUserMessage(@Nullable String user, @Nonnull String sessionID, @Nonnull String message) throws CreditsException, NotificationException;

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to check
     * that a User is enabled to open a new push Session. If the check
     * succeeds, this also notifies the Metadata Adapter that the Session
     * is being assigned to the User. <BR>
     * Request context information is also available; this allows for
     * differentiating group, schema and message management based on specific
     * Request characteristics. <BR>
     * This method runs in the Server thread pool specific
     * for the Adapter Set, if defined.
     * 
     * @param user A User name.
     * @param sessionID The ID of a new Session.
     * @param clientContext
     * A value object that contains name-value
     * pairs with various information about the request context.
     * All values are supplied as strings. Information related to a client
     * connection refers to the HTTP request that originated the call.
     * Available keys are:
     * <UL>
     * <LI>"REMOTE_IP" - string representation of the remote IP
     * related to the current connection; it may be a proxy address</LI>
     * <LI>"REMOTE_PORT" - string representation of the remote port
     * related to the current connection</LI>
     * <LI>"USER_AGENT" - the user-agent as declared in the current
     * connection HTTP header</LI>
     * <LI>"FORWARDING_INFO" - the content of the X-Forwarded-For
     * HTTP header related to the current connection; intermediate proxies
     * usually set this header to supply connection routing information</LI>
     * <LI>"LOCAL_SERVER" - the name of the specific server socket
     * that handles the current connection, as configured through the
     * &lt;http_server&gt; or &lt;https_server&gt; element</LI>
     * <LI>"CLIENT_TYPE" - the type of client API in use. The value may be null
     * for some old client APIs</LI>
     * <LI>"CLIENT_VERSION" - the signature, including version and build number,
     * of the client API in use. The signature may be only partially complete,
     * or even null, for some old client APIs and for some custom clients</LI>
     * <LI>"REQUEST_ID" - the same id that has just been supplied
     * to NotifyUser for the current client request instance;
     * this allows for using local authentication-related details for
     * the authorization task.
     * Note: the Remote Adapter is responsible for disposing any cached
     * information in case NotifyNewSession is not called because of any
     * early error during request management.</LI>
     * </UL>
     * 
     * @exception CreditsException
     * in case the User is not enabled to open the new Session.
     * If it's possible that the User would be enabled as soon as
     * another Session were closed, then a ConflictingSessionException
     * can be thrown, in which the ID of the other Session must be
     * specified.
     * In this case, a second invocation of the method with the same
     * "REQUEST_ID" and a different Session ID will be received.
     * 
     * @exception NotificationException
     * in case something is wrong in the parameters, such as the ID
     * of a Session already open for this or a different User.
     * 
     */
    void notifyNewSession(@Nullable String user, @Nonnull String sessionID, @Nonnull Map<String,String> clientContext) throws CreditsException, NotificationException;
    
    /**
     * Called by Lightstreamer Kernel through the Remote Server to ask
     * for an optional time-to-live setting for a session just started.
     * If this setting is not needed, the method can be left unimplemented
     * (sticking to its default implementation, which poses no limit).
     * <BR>If the session is terminated due to this setting, the originating
     * client will receive the notification of the termination according with the API
     * in use, together with a proper cause code.
     * 
     * @param user A User name.
     * @param session A session ID.
     * @return The time-to-live setting to be applied to the specified session,
     * as a positive number of seconds. If zero or negative, no time-to-live
     * limit will be applied.
     */
    default int getSessionTimeToLive(@Nullable String user, @Nonnull String session) {
        return 0;
    }

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to notify
     * the Metadata Adapter that a push Session has been closed. <BR>
     * This method is called by the Server asynchronously
     * and does not consume a pooled thread on the Server.
     * As a consequence, it is not guaranteed that no more calls related with
     * this sessionID, like notifyNewTables, notifyTablesClose, and getItems
     * can occur after its invocation on parallel threads. Accepting them
     * would have no effect.
     * However, if the method may have side-effects on the Adapter, like notifyUserMessage,
     * the Adapter is responsible for checking if the session is still valid.
     * 
     * @param sessionID A Session ID.
     * @exception NotificationException
     * in case something is wrong in the parameters, such as the ID of a Session 
     * that is not currently open.
     */
    void notifySessionClose(@Nonnull String sessionID) throws NotificationException;

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to know
     * whether the Metadata Adapter must or must not be notified any time a Table
     * (i&#46;e&#46;: Subscription) is added or removed from a push Session owned by a supplied User. If this method returns 
     * false, the methods NotifyNewTables and NotifyTablesClose will never be called for this User, saving 
     * some processing time. In this case, the User will be allowed to add to his Sessions any Tables
     * (i&#46;e&#46;: Subscriptions) he wants. <BR>
     * This method runs in the Server authentication thread pool, if defined.
     * 
     * @param user A User name.
     * @return True if the Metadata Adapter must be notified any time a Table (i&#46;e&#46;: Subscription)
     * is added or removed from a Session owned by the User.
    */
    boolean wantsTablesNotification(@Nullable String user);

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to check
     * that a User is enabled to add some Tables (i&#46;e&#46;: Subscriptions) to a push Session. 
     * If the check succeeds, this also notifies the Metadata Adapter that the Tables are being added to the 
     * Session. <BR>
     * The method is invoked only if enabled for the User through wantsTablesNotification. <BR>
     * This method runs in the Server thread pool specific
     * for the Data Adapter that supplies the involved items, if defined.
     * 
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param tables An array of TableInfo instances, each of them containing the details of a Table 
     * (i&#46;e&#46;: Subscription) to be added to the Session.
     * The elements in the array represent Tables (i&#46;e&#46;: Subscriptions) whose
     * subscription is requested atomically by the client. A single element
     * should be expected in the array, unless clients based on a very old
     * version of a client library or text protocol may be in use.
     * @exception CreditsException in case the User is not allowed to add the specified Tables (i&#46;e&#46;: Subscriptions) to the Session.
     * @exception NotificationException in case something is wrong in the
     * parameters, such as the ID of a Session that is not currently open
     * or inconsistent information about a Table (i&#46;e&#46;: Subscription).
     * 
    */
    void notifyNewTables(@Nullable String user, @Nonnull String sessionID, @Nonnull TableInfo[] tables) throws NotificationException, CreditsException;

    /** 
     * Called by the Remote Server to know whether the Metadata Adapter wants to have
     * the possibility to force the unsubscription
     * (through {@link MetadataControlListener#forceUnsubscription(String, TableInfo)})
     * of some Tables (i&#46;e&#46;: Subscriptions) just being subscribed. In fact, since
     * enabling this possibility requires added resources on the Proxy Adapter,
     * this should be explicitly requested for every subscription.
     * Hence this method is invoked just after a successful invocation of
     * {@link #notifyNewTables(String, String, TableInfo[])}, and provides the same
     * sessionID and tables arguments.
     * If unsubscription support is not needed, the method can be left unimplemented
     * (sticking to its default implementation).
     * <BR>Note: the unsubscription is supported only if the array of TableInfo
     * has a single element; hence this method is invoked only in this case.
     * However, the case of arrays with multiple elements is only possible when
     * extremely old client SDKs are in use.
     * 
     * @param sessionID The ID of a Session 
     * @param tables An array of TableInfo instances, each of them containing
     * the details of a Table (i&#46;e&#46;: Subscription) being added to the Session.
     * Actually, the length of the array will be always 1.
     * @return True if the Metadata Adapter wants to have the possibility to force
     * the unsubscription of these Tables (i&#46;e&#46;: Subscriptions).
    */
    default boolean enableTableUnsubscription(@Nonnull String sessionID, @Nonnull TableInfo[] tables) {
        return false;
    }

    /** 
     * Called by the Remote Server to know whether the Metadata Adapter wants to
     * receive (in {@link #notifyTablesClose(String, TableInfo[])}) final traffic
     * statistics on the Items of some Tables (i&#46;e&#46;: Subscriptions) just being
     * subscribed.
     * Hence this method is invoked just after a successful invocation of
     * {@link #notifyNewTables(String, String, TableInfo[])}, and provides the same
     * sessionID and tables arguments.
     * If traffic statistics are not needed, the method can be left unimplemented
     * (sticking to its default implementation).
     * 
     * @param sessionID The ID of a Session 
     * @param tables An array of TableInfo instances, each of them containing
     * the details of a Table (i&#46;e&#46;: Subscription) being added to the Session.
     * @return True if the Metadata Adapter wants to receive final traffic
     * statistics on the Items of these Tables (i&#46;e&#46;: Subscriptions).
    */
    default boolean wantsFinalTableStatistics(@Nonnull String sessionID, @Nonnull TableInfo[] tables) {
        return false;
    }

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to notify
     * the Metadata Adapter that some Tables (i&#46;e&#46;: Subscriptions) have been removed from
     * a push Session. <BR>
     * The method is invoked only if enabled for the User through wantsTablesNotification. <BR>
     * This method is called by the Server asynchronously
     * and does not consume a pooled thread on the Server.
     * 
     * @param sessionID A Session ID.
     * @param tables An array of TableInfo instances, each of them containing the details of a Table 
     * (i&#46;e&#46;: Subscription) that has been removed from the Session.
     * The supplied array is in 1:1 correspondance with the array supplied by
     * notifyNewTables in a previous call;
     * the correspondance can be recognized by matching the getWinIndex return value
     * of the included TableInfo objects (if multiple objects are included,
     * it must be the same for all of them).
     * @exception NotificationException in case something is wrong in the parameters, such as the ID of a Session 
     * that is not currently open or a Table (i&#46;e&#46;: Subscription) that is not contained in the Session.
     * 
    */
    void notifyTablesClose(@Nonnull String sessionID, @Nonnull TableInfo[] tables) throws NotificationException;

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to check that a User is enabled to access
     * the specified MPN device. The success of this method call is
     * a prerequisite for all MPN operations, including the activation of a
     * subscription, the deactivation of a subscription, the change of a device
     * token, etc. Some of these operations have a subsequent specific notification,
     * i&#46;e&#46; notifyMpnSubscriptionActivation and notifyMpnDeviceTokenChange. 
     * <BR>
     * Take particular precautions when authorizing device access, if
     * possible ensure the user is entitled to the specific platform, 
     * device token and application ID.
     * 
     * <br>
     * <b>Edition Note:</b>
     * <br> Push Notifications is an optional feature,
     * available depending on Edition and License Type.
	 * To know what features are enabled by your license, please see the License
	 * tab of the Monitoring Dashboard (by default, available at /dashboard).
     * 
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param device Specifies an MPN device.
     * @exception CreditsException if the User is not allowed to access the
     * specified MPN device in the Session.
     * @exception NotificationException if something is wrong in the parameters,
     * such as inconsistent information about the device.
    */
    void notifyMpnDeviceAccess(@Nullable String user, @Nonnull String sessionID, @Nonnull MpnDeviceInfo device) throws CreditsException, NotificationException;

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to check that a User is enabled 
     * to activate a Push Notification subscription.
     * If the check succeeds, this also notifies the Metadata Adapter that
     * Push Notifications are being activated.
     * <BR>
     * Take particular precautions when authorizing subscriptions, if
     * possible check for validity the trigger expression reported by 
     * MpnSubscriptionInfo.Trigger, as it may contain maliciously 
     * crafted code. The MPN notifiers configuration file contains a first-line 
     * validation mechanism based on regular expression that may also be used 
     * for this purpose.
     * 
     * <br>
     * <b>Edition Note:</b>
     * <br> Push Notifications is an optional feature,
     * available depending on Edition and License Type.
	 * To know what features are enabled by your license, please see the License
	 * tab of the Monitoring Dashboard (by default, available at /dashboard).
     * 
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User. The session ID is
     * provided for a thorough validation of the Table information, but Push 
     * Notification subscriptions are persistent and survive the session. Thus,
     * any association between this Session ID and this Push Notification
     * subscription should be considered temporary.
     * @param table A TableInfo instance, containing the details of a Table 
     * (i&#46;e&#46;: Subscription) for which Push Notification have to be activated.
     * @param mpnSubscription An MpnSubscriptionInfo instance, containing the
     * details of a Push Notification to be activated.
     * @exception CreditsException if the User is not allowed to activate the
     * specified Push Notification in the Session.
     * @exception NotificationException if something is wrong in the parameters,
     * such as inconsistent information about a Table (i&#46;e&#46;: Subscription) or 
     * a Push Notification.
    */
    void notifyMpnSubscriptionActivation(@Nullable String user, @Nonnull String sessionID, @Nonnull TableInfo table, @Nonnull MpnSubscriptionInfo mpnSubscription) throws CreditsException, NotificationException;

    /** 
     * Called by Lightstreamer Kernel through the Remote Server to check that a User is enabled to change
     * the token of an MPN device. 
     * If the check succeeds, this also notifies the Metadata Adapter that future
     * client requests should be issued by specifying the new device token.
     * <BR>
     * Take particular precautions when authorizing device token changes,
     * if possible ensure the user is entitled to the new device token.
     * 
     * <br>
     * <b>Edition Note:</b>
     * <br>Push Notifications is an optional feature,
     * available depending on Edition and License Type.
	 * To know what features are enabled by your license, please see the License
	 * tab of the Monitoring Dashboard (by default, available at /dashboard).
     * 
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param device Specifies an MPN device.
     * @param newDeviceToken The new token being assigned to the device.
     * @exception CreditsException if the User is not allowed to change the
     * specified device token.
     * @exception NotificationException if something is wrong in the parameters,
     * such as inconsistent information about the device.
    */
    void notifyMpnDeviceTokenChange(@Nullable String user, @Nonnull String sessionID, @Nonnull MpnDeviceInfo device, @Nonnull String newDeviceToken) throws CreditsException, NotificationException;
}