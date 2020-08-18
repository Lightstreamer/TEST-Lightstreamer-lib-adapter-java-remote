# Lightstreamer Changelog - SDK for Java Remote Adapters

## [current state]

**Improvements**

- Modified the reference to the external package for the log support, which is now available at<br/>
https://mvnrepository.com/artifact/com.lightstreamer/ls-log-adapter-java<br/>
with documentation available at<br/>
https://sdk.lightstreamer.com/ls-log-adapter-java/1.0.2/api/com/lightstreamer/log/LoggerProvider.html

- Added missing references to the log resources and log details in the setLoggetProvider docs.
   
## [1.4.0] (09-06-2020)

*Compatible with Adapter Remoting Infrastructure since 1.8.*  
*May not be compatible with code developed with the previous version; see compatibility notes below.*

**New Features**

- Made the library available on the public Maven repository, at the following address:<br/>
https://mvnrepository.com/artifact/com.lightstreamer/ls-adapter-remote<br/>
Previous releases were included in Lightstreamer distribution packages, up to Lightstreamer version 7.1.1.

- Made the library open source and available on GitHub at the following address:<br/>
https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-remote

**Improvements**

- Resorted to an external package for the log support. The new library is available at<br/>
https://lightstreamer.com/repo/maven/com/lightstreamer/ls-log-adapter-java<br/>
See the documentation at<br/>
https://lightstreamer.com/repo/maven/com/lightstreamer/ls-log-adapter-java/1.0.2/ls-log-adapter-java-1.0.2-javadoc.jar%23/<br/>
As a consequence, the signature of setLoggerProvider has changed and the whole "com.lightstreamer.adapters.remote.log" package has been removed.<br/>
**COMPATIBILITY NOTE:** *Custom code using setLoggerProvider and the LoggerProvider interface should be revised, based on the new documentation.*
   
- Added the null annotations (according to JSR 305) in the class files of public classes, to better support library use with Kotlin and any other language which leverages JSR 305.<br/>
**COMPATIBILITY NOTE:** *Existing code written in Kotlin and similar languages may no longer compile and should be aligned with the new method signatures. No issues are expected for existing Java code.*

## [1.3.1] (24-01-2020)

*Compatible with Adapter Remoting Infrastructure since 1.8.*  
*Compatible with code developed with the previous version.*

**New Features**

- Extended DataProviderServer and MetadataProviderServer (through the Server superclass) with settings of credentials, to be sent to the Proxy Adapter upon each connection.
Credential check is an optional configuration of the Proxy Adapter; if not leveraged, the credentials will be ignored.

- Added full support for ARI Protocol extensions introduced in Adapter Remoting Infrastructure version 1.9.<br/>
**COMPATIBILITY NOTE:** *If Adapter Remoting Infrastructure 1.8.x (corresponding to Server 7.0.x) is used and credentials to be sent to the Proxy Adapter are specified, they will obviously be ignored, but the Proxy Adapter will issue some log messages at WARN level on startup.*<br/>
**COMPATIBILITY NOTE:** *Only in the very unlikely case that Adapter Remoting Infrastructure 1.8.x (corresponding to Server 7.0.x) were used and a custom remote parameter named "ARI.version" were defined in adapters.xml, this SDK would not be compatible with Lightstreamer Server, hence the Server should be upgraded (or a different parameter name should be used).*

**Improvements**

- Modified the handling of the keepalives when connected to a Proxy Adapter (i.e. Adapter Remoting Infrastructure) version 1.9 or higher: the preferred keepalive interval requested by the Proxy Adapter, when stricter than the configured one, is now obeyed (with a safety minimun of 1 second).
Moreover, in that case, the default interval when not configured is now 10 seconds instead of 1.<br/>
**COMPATIBILITY NOTE:** *If an existing installation relies on a very short keepalive interval to keep the connection alive due to intermediate nodes, the time should now be explicitly configured.*

- Deprecated the constructors of DataProviderServer and MetadataProviderServer that allow for the specification of initializeOnStart as true.
These constructors will be removed in a future update, as the initializeOnStart flag was just meant as a temporary backward compatibility trick.<br/>
**COMPATIBILITY NOTE:** *Existing code and binaries using the deprecated constructors are still supported, but it is recommended to align the code. See the notes in the constructor documentations for details.
*Improved the documentation of the start method of the DataProviderServer and MetadataProviderServer classes, to clarify the behavior.*

- Added clarifications in the documentation of the exception handlers and fixed a few obsolete notes.

- Discontinued the support for java 7 SDK and runtime environment. Java 8 or later is now required.<br/>
**COMPATIBILITY NOTE:** *Installations of Remote Adapters still based on the discontinued java 7 JVM have to be upgraded. Existing Adapters that were compiled for a java 7 or earlier JVM don't need to be upgraded.*

**Bug Fixes**

- Fixed a bug in the default implementation of handleIOException (see setExceptionHandler and the ExceptionHandler interface), which could have raised, in turn, a NullPointerException, preventing it from terminating the process.

## [1.2.0] (20-12-2017)

*Compatible with Adapter Remoting Infrastructure since 1.8.*  
*May not be compatible with code developed with the previous version; see compatibility notes below.*

**New Features**

- Modified the interface in the part related to Mobile Push Notifications, after the full revision of Lightstreamer Server's MPN Module. In particular:
	- Modified the signature of the notifyMpnDeviceAccess and notifyMpnDeviceTokenChange methods of the MetadataProvider interface, to add a session ID argument.<br/>
	**COMPATIBILITY NOTE:** *Existing Remote Metadata Adapter source code has to be ported in order to be compiled with the new jar, unless the Adapter class inherits from the supplied MetadataProviderAdapter or LiteralBasedProvider and the above methods are not defined.<br/>
	*On the other hand, existing Remote Metadata Adapter binaries are still supported as long as the MPN Module is disabled. Otherwise, they should be recompiled after porting.*
	- Revised the public constants defined in the MpnPlatformType class. The constants referring to the supported platforms have got new names and corresponding new values, whereas the constants for platforms not yet supported have been removed.<br/>
	**COMPATIBILITY NOTE:** *Existing Remote Metadata Adapters explicitly referring to the constants have to be aligned.
	*Even if just testing the values of the MpnPlatformType objects received from MpnDeviceInfo.getType, they still have to be aligned.*
	- Removed the subclasses of MpnSubscriptionInfo (namely MpnApnsSubscriptionInfo and MpnGcmSubscriptionInfo) that were used by the SDK library to supply the attributes of the MPN subscriptions in notifyMpnSubscriptionActivation. Now, simple instances of
	MpnSubscriptionInfo will be supplied and attribute information can be obtained through the new getNotificationFormat method.
	See the MPN chapter on the General Concepts document for details on the characteristics of the Notification Format.<br/>
	**COMPATIBILITY NOTE:** *Existing Remote Metadata Adapters leveraging notifyMpnSubscriptionActivation and inspecting the supplied MpnSubscriptionInfo have to be ported to the new class contract.*
	- Added equality checks based on the content in MpnDeviceInfo and MpnSubscriptionInfo. Fixed hashCode in MpnPlatformType to comply with the equality check.
	- Improved the interface documentation in various parts.
	
**Improvements**

- Added checks to protect the MetadataProviderServer and DataProviderServer objects from reuse, which is forbidden.

- Clarified in the docs for notifySessionClose which race conditions with other methods can be expected.

- Aligned the documentation to comply with current licensing policies.

## [1.1.2] (23-01-2017)

*Compatible with Adapter Remoting Infrastructure since 1.7.*  
*Compatible with code developed with the previous version.*

**Improvements**

- Improved the Javadocs, by shortening the concise descriptions of some classes and methods.

**Bug Fixes**

- Fixed a problem introduced by a known Java compiler bug, which caused a "verify error" to be reported at runtime.

## [1.1.1] (05-09-2016)

*Compatible with Adapter Remoting Infrastructure since 1.7.*  
*Compatible with code developed with the previous version.*

**Improvements**

- Added meta-information on method argument names for interface classes, so that developer GUIs can take advantage of them.

## [1.1.0] (10-05-2016)

*Compatible with Adapter Remoting Infrastructure since 1.7.*  
*May not be compatible with code developed with the previous version; see compatibility notes below.*

**New Features**

- Introduced parallelization of the invocations of methods on the Remote Metadata Adapter; in fact, previously, the invocations were always done sequentially, with possible inefficient use of the available resources (invocations for the Data Adapter were already done in parallel).
Also introduced suitable configuration; see the docs for MetadataProviderServer and DataProviderServer for details.<br/>
**COMPATIBILITY NOTE:** *If existing Remote Metadata Adapters don't support concurrent invocations, sequential invocations should be restored by configuration.*

- Introduced the possibility to configure the keepalive time (which was fixed to one second) through the custom "lightstreamer.keepalive.millis" system property.

**Improvements**

- Improved logging; now the detailed log of request-reply messages not including notification messages (i.e. data updates) is possible. Moreover, the keepalives can now be excluded from the the detailed log of request, reply and notification messages.

- Added missing javadocs for the RemotingException class and a few other fields.<br/>
Fixed some mistaken class names and other typos in the javadocs comments. Also removed the spurious DataProviderProtocol docs.<br/>
Revised javadoc formatting style and fixed a formatting error in DataProvider class.<br/>
Fixed the Javadocs for DataProvider and MetadataProvider interfaces on the way implementations are supplied.

**Bug Fixes**

- Fixed the API documentation of class Server, by removing the method "init". In fact, the method was not meant to be public.

## [1.0.2] (16-07-2015)

*Compatible with Adapter Remoting Infrastructure since 1.7.*  
*Compatible with code developed with the previous version.*

**Improvements**

- Fixed the handling of generic exceptions thrown by a DataProvider or MetadataProvider implementation: generic exceptions are now forwarded to the connected Lightstreamer Server whenever possible.

## [1.0.1] (21-01-2015)

*Compatible with Adapter Remoting Infrastructure since 1.7.*  

- Included in Lightstreamer distribution. The features are similar to the current SDK for .NET Adapters (version 1.9 build 1008).
