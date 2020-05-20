# Lightstreamer Java Remote Adapter SDK

This project includes the source code of the Lightstreamer .Net Remote Adapter. This resource is needed to develop Remote Data Adapters and Remote Metadata Adapters for [Lightstreamer Server](http://www.lightstreamer.com/) with Java.

Each Lightstreamer session requires the presence of an Adapter Set, which is made up of one Metadata Adapter and one or multiple Data Adapters. Multiple Adapter Sets can be plugged onto Lightstreamer Server.
The adapters will run in a separate process, communicating with the Server through corresponding `Proxy Adapters` embedded in the Lightstreamer server.

This SDK is designed for Java 8 and greater.

### The ARI Architecture

Lightstreamer Server exposes native Java In-Process Adapter interfaces. The remote interfaces are added through the [Lightstreamer Adapter Remoting Infrastructure (**ARI**)](https://lightstreamer.com/docs/remoting_base/Adapter%20Remoting%20Infrastructure.pdf). 

![architecture](architecture.png)

ARI is simply made up of two types of Proxy Adapters and a *Network Protocol*. The two Proxy Adapters, one implementing the Data Adapter interface and the other implementing the Metadata Adapter interface, are meant to be plugged into Lightstreamer Kernel.

Basically, a Proxy Adapter exposes the Adapter interface through TCP sockets. In other words, it offers a Network Protocol, which any remote counterpart can implement to behave as a Lightstreamer Data Adapter or Metadata Adapter. This means you can write a remote Adapter in any language, provided that you have access to plain TCP sockets.
But, if your remote Adapter is based on certain languages/technologies (such as Java, .NET, and Node.js), you can forget about direct socket programming, and leverage a ready-made library that exposes a higher level interface. Now, you will simply have to implement this higher level interface.<br>

In this specific project we provide the full sorce code that makes up the <b>Lightstreamer Java Remote Adapter API</b> library.

## Compatibility ##

The library is compatible with [Adapter Remoting Infrastructure](https://lightstreamer.com/docs/adapter_generic_base/ARI%20Protocol.pdf) since version 1.8.

## External Links

- [Maven repository](https://www.lightstreamer.com/repo/maven/)

- [Examples](https://demos.lightstreamer.com/?p=lightstreamer&t=adapter&sadapterjava=remote)

- [API Reference](https://lightstreamer.com/docs/adapter_java_remote_api/index.html)

## Other GitHub projects using this library

- [Lightstreamer - Reusable Metadata Adapters - Java Remote Adapter ](https://github.com/Lightstreamer/Lightstreamer-example-ReusableMetadata-adapter-java-remote)

## Support

For questions and support please use the [Official Forum](https://forums.lightstreamer.com/). The issue list of this page is **exclusively** for bug reports and feature requests.

## License

[Apache 2.0](https://opensource.org/licenses/Apache-2.0)
