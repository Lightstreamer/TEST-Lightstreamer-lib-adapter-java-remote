# Lightstreamer Java Remote Adapter SDK

This project includes the source code of the Lightstreamer Java Remote Adapter API. This resource is needed to develop Remote Data Adapters and Remote Metadata Adapters for [Lightstreamer Server](http://www.lightstreamer.com/) with Java.

Each Lightstreamer session requires the presence of an Adapter Set, which is made up of one Metadata Adapter and one or multiple Data Adapters. Multiple Adapter Sets can be plugged onto Lightstreamer Server.
The adapters will run in a separate process, communicating with the Server through corresponding `Proxy Adapters` embedded in the Lightstreamer server.

This SDK is designed for Java 8 and greater.

### The ARI Architecture

Lightstreamer Server exposes native Java In-Process Adapter interfaces. The remote interfaces are added through the [Lightstreamer Adapter Remoting Infrastructure (**ARI**)](https://lightstreamer.com/docs/ls-ARI/latest/Adapter%20Remoting%20Infrastructure.pdf). 

![architecture](architecture.png)

ARI is simply made up of two types of Proxy Adapters and a *Network Protocol*. The two Proxy Adapters, one implementing the Data Adapter interface and the other implementing the Metadata Adapter interface, are meant to be plugged into Lightstreamer Kernel.

Basically, a Proxy Adapter exposes the Adapter interface through TCP sockets. In other words, it offers a Network Protocol, which any remote counterpart can implement to behave as a Lightstreamer Data Adapter or Metadata Adapter. This means you can write a remote Adapter in any language, provided that you have access to plain TCP sockets.
But, if your remote Adapter is based on certain languages/technologies (such as Java, .NET, and Node.js), you can forget about direct socket programming, and leverage a ready-made library that exposes a higher level interface. Now, you will simply have to implement this higher level interface.<br>

In this specific project we provide the full sorce code that makes up the <b>Lightstreamer Java Remote Adapter API</b> library.

## Compatibility

The library is compatible with [Adapter Remoting Infrastructure](https://www.lightstreamer.com/api/ls-generic-adapter/latest/ARI%20Protocol.pdf) since version 1.8.

## Using the API

Since the API is available from the Maven Central Repository, to setup your development environment add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.lightstreamer</groupId>
    <artifactId>ls-adapter-remote</artifactId>
    <version>1.4.0</version>
</dependency>
```

## External Links

- [Maven repository](https://mvnrepository.com/artifact/com.lightstreamer/ls-adapter-remote/)

- [Examples](https://demos.lightstreamer.com/?p=lightstreamer&t=adapter&sadapterjava=remote)

- [API Reference](https://www.lightstreamer.com/api/ls-adapter-remote/latest/)

## Other GitHub projects using this library

- [Lightstreamer - Reusable Metadata Adapters - Java Remote Adapter ](https://github.com/Lightstreamer/Lightstreamer-example-ReusableMetadata-adapter-java-remote)

## Support

For questions and support please use the [Official Forum](https://forums.lightstreamer.com/). The issue list of this page is **exclusively** for bug reports and feature requests.

## License

[Apache 2.0](https://opensource.org/licenses/Apache-2.0)
