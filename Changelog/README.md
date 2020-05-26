# Lightstreamer Changelog - SDK for Java Remote Adapters



## 1.4.0 - <i>to be released</i>

<i>Compatible with Adapter Remoting Infrastructure since 1.8.</i><br/>
<i>May not be compatible with code developed with the previous version; see compatibility notes below.</i>

Resorted to an external package for the log support.<br/>
<b>COMPATIBILITY NOTE:</b> <i>Custom code
using the LoggerProvider interface should be revised, based on the new documentation;
see setLoggerProvider.</i>
   
Added the null annotations (according to JSR 305) in the class files of public
classes, to better support library use with Kotlin and any other language which
leverages JSR 305.<br/>
<b>COMPATIBILITY NOTE:</b> <i>Existing code written in Kotlin
and similar languages may no longer compile and should be aligned with the new
method signatures. No issues are expected for existing Java code.</i>


## 1.3.1 - <i>Released on 24 Jan 2020</i>

<i>Compatible with Adapter Remoting Infrastructure since 1.8.</i><br/>
<i>Compatible with code developed with the previous version.</i>

Extended DataProviderServer and MetadataProviderServer (through the Server superclass)
with settings of credentials, to be sent to the Proxy Adapter upon each connection.
Credential check is an optional configuration of the Proxy Adapter; if not leveraged,
the credentials will be ignored.
   
Modified the handling of the keepalives when connected to a Proxy Adapter
(i.e. Adapter Remoting Infrastructure) version 1.9 or higher:
the preferred keepalive interval requested by the Proxy Adapter, when stricter
than the configured one, is now obeyed (with a safety minimun of 1 second).
Moreover, in that case, the default interval when not configured is now 10 seconds
instead of 1.<br/>
<b>COMPATIBILITY NOTE:</b> <i>If an existing installation relies
on a very short keepalive interval to keep the connection alive due to intermediate
nodes, the time should now be explicitly configured.</i>

Added full support for ARI Protocol extensions introduced in Adapter Remoting Infrastructure
version 1.9.<br/>
<b>COMPATIBILITY NOTE:</b> <i>If Adapter Remoting Infrastructure 1.8.x
(corresponding to Server 7.0.x) is used and credentials to be sent to the Proxy Adapter
are specified, they will obviously be ignored, but the Proxy Adapter will issue some
log messages at WARN level on startup.</i><br/>
<b>COMPATIBILITY NOTE:</b> <i>Only in the very unlikely case
that Adapter Remoting Infrastructure 1.8.x (corresponding to Server 7.0.x) were used
and a custom remote parameter named "ARI.version" were defined in adapters.xml,
this SDK would not be compatible with Lightstreamer Server, hence the Server should be upgraded
(or a different parameter name should be used).</i>

Deprecated the constructors of DataProviderServer and MetadataProviderServer
that allow for the specification of initializeOnStart as true.
These constructors will be removed in a future update, as the initializeOnStart
flag was just meant as a temporary backward compatibility trick.<br/>
<b>COMPATIBILITY NOTE:</b> <i>Existing code and binaries
using the deprecated constructors are still supported, but it is recommended
to align the code. See the notes in the constructor documentations for details.
Improved the documentation of the start method of the DataProviderServer and
MetadataProviderServer classes, to clarify the behavior.</i>

Fixed a bug in the default implementation of handleIOException (see
setExceptionHandler and the ExceptionHandler interface), which could have raised,
in turn, a NullPointerException, preventing it from terminating the process.

Added clarifications in the documentation of the exception handlers and fixed
a few obsolete notes.

Discontinued the support for java 7 SDK and runtime environment.
Java 8 or later is now required.
<b>COMPATIBILITY NOTE:</b> <i>Installations of Remote Adapters
still based on the discontinued java 7 JVM have to be upgraded. Existing Adapters
that were compiled for a java 7 or earlier JVM don't need to be upgraded.</i>


## 1.2.0 - <i>Released on 20 Dec 2017</i>

<i>Compatible with Adapter Remoting Infrastructure since 1.8.</i><br/>
<i>May not be compatible with code developed with the previous version; see compatibility notes below.</i>

Modified the interface in the part related to Mobile Push Notifications,
after the full revision of Lightstreamer Server's MPN Module. In particular:
 - Modified the signature of the notifyMpnDeviceAccess and
notifyMpnDeviceTokenChange methods of the MetadataProvider interface,
to add a session ID argument.<br/>
<b>COMPATIBILITY NOTE:</b> <i>Existing Remote Metadata Adapter
source code has to be ported in order to be compiled with the new jar,
unless the Adapter class inherits from the supplied MetadataProviderAdapter
or LiteralBasedProvider and the above methods are not defined.<br/>
On the other hand, existing Remote Metadata Adapter binaries are still supported
as long as the MPN Module is disabled.
Otherwise, they should be recompiled after porting.</i>
 - Revised the public constants defined in the MpnPlatformType class.
The constants referring to the supported platforms have got new names and
corresponding new values, whereas the constants for platforms not yet
supported have been removed.<br/>
<b>COMPATIBILITY NOTE:</b> <i>Existing Remote Metadata Adapters
explicitly referring to the constants have to be aligned.
Even if just testing the values of the MpnPlatformType objects received
from MpnDeviceInfo.getType, they still have to be aligned.</i>
 - Removed the subclasses of MpnSubscriptionInfo (namely
MpnApnsSubscriptionInfo and MpnGcmSubscriptionInfo) that were used
by the SDK library to supply the attributes of the MPN subscriptions
in notifyMpnSubscriptionActivation. Now, simple instances of
MpnSubscriptionInfo will be supplied and attribute information can be
obtained through the new getNotificationFormat method.
See the MPN chapter on the General Concepts document for details on the
characteristics of the Notification Format.<br/>
<b>COMPATIBILITY NOTE:</b> <i>Existing Remote Metadata Adapters
leveraging notifyMpnSubscriptionActivation and inspecting the supplied
MpnSubscriptionInfo have to be ported to the new class contract.</i>
 - Added equality checks based on the content in MpnDeviceInfo and MpnSubscriptionInfo.
Fixed hashCode in MpnPlatformType to comply with the equality check.
 - Improved the interface documentation in various parts.

Added checks to protect the MetadataProviderServer and DataProviderServer objects
from reuse, which is forbidden.

Clarified in the docs for notifySessionClose which race conditions with other
methods can be expected.

Aligned the documentation to comply with current licensing policies.


## 1.1.2 - <i>Released on 23 Jan 2017</i>

<i>Compatible with Adapter Remoting Infrastructure since 1.7.</i><br/>
<i>Compatible with code developed with the previous version.</i>

Fixed a problem introduced by a known Java compiler bug, which caused a "verify error"
to be reported at runtime. The problem only affected the previous build 24.

Improved the Javadocs, by shortening the concise descriptions of some classes
and methods.


## 1.1.1 - <i>Released on 5 Sep 2016</i>

<i>Compatible with Adapter Remoting Infrastructure since 1.7.</i><br/>
<i>Compatible with code developed with the previous version.</i>

Added meta-information on method argument names for interface classes,
so that developer GUIs can take advantage of them.


## 1.1.0 - <i>Released on 10 May 2016</i>

<i>Compatible with Adapter Remoting Infrastructure since 1.7.</i><br/>
<i>May not be compatible with code developed with the previous version; see compatibility notes below.</i>

Introduced parallelization of the invocations of methods on the Remote
Metadata Adapter; in fact, previously, the invocations were always done
sequentially, with possible inefficient use of the available resources
(invocations for the Data Adapter were already done in parallel).
Also introduced suitable configuration; see the docs for
MetadataProviderServer and DataProviderServer for details.
<b>COMPATIBILITY NOTE:</b> <i>If existing Remote
Metadata Adapters don't support concurrent invocations, sequential
invocations should be restored by configuration.</i>

Introduced the possibility to configure the keepalive time (which was fixed
to one second) through the custom "lightstreamer.keepalive.millis"
system property.

Fixed the API documentation of class Server, by removing the method "init".
In fact, the method was not meant to be public.

Improved logging; now the detailed log of request-reply messages not including
notification messages (i.e. data updates) is possible. Moreover, the keepalives
can now be excluded from the the detailed log of request, reply and notification
messages.

Added missing javadocs for the RemotingException class and a few other fields.<br/>
Fixed some mistaken class names and other typos in the javadocs comments.
Also removed the spurious DataProviderProtocol docs.<br/>
Revised javadoc formatting style and fixed a formatting error in DataProvider class.<br/>
Fixed the Javadocs for DataProvider and MetadataProvider interfaces on the way
implementations are supplied.


## 1.0.2 - <i>Released on 16 Jul 2015</i>

<i>Compatible with Adapter Remoting Infrastructure since 1.7.</i><br/>
<i>Compatible with code developed with the previous version.</i>

Fixed the handling of generic exceptions thrown by a DataProvider or MetadataProvider
implementation: generic exceptions are now forwarded to the connected Lightstreamer Server
whenever possible.


## 1.0.1 - <i>Released on 21 Jan 2015</i>

<i>Compatible with Adapter Remoting Infrastructure since 1.7.</i><br/>

Included in Lightstreamer distribution. The features are similar
to the current SDK for .NET Adapters (version 1.9 build 1008).
