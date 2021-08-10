# Lightstreamer Java Remote Adapter SDK

This project includes the source code of the Lightstreamer Java Remote Adapter API. This resource is needed to develop Remote Data Adapters and Remote Metadata Adapters for [Lightstreamer Server](http://www.lightstreamer.com/) with Java.

Each Lightstreamer session requires the presence of an Adapter Set, which is made up of one Metadata Adapter and one or multiple Data Adapters. Multiple Adapter Sets can be plugged onto Lightstreamer Server.
The adapters will run in a separate process, communicating with the Server through corresponding `Proxy Adapters` embedded in the Lightstreamer server.

This SDK is designed for Java 8 and greater.

### The ARI Architecture

Lightstreamer Server exposes native Java In-Process Adapter interfaces. The remote interfaces are added through the [Lightstreamer Adapter Remoting Infrastructure (**ARI**)](https://lightstreamer.com/api/ls-generic-adapter/latest/ARI%20Protocol.pdf). 

![architecture](architecture.png)

ARI is simply made up of two types of Proxy Adapters and a *Network Protocol*. The two Proxy Adapters, one implementing the Data Adapter interface and the other implementing the Metadata Adapter interface, are meant to be plugged into Lightstreamer Kernel.

Basically, a Proxy Adapter exposes the Adapter interface through TCP sockets. In other words, it offers a Network Protocol, which any remote counterpart can implement to behave as a Lightstreamer Data Adapter or Metadata Adapter. This means you can write a remote Adapter in any language, provided that you have access to plain TCP sockets.
But, if your remote Adapter is based on certain languages/technologies (such as Java, .NET, and Node.js), you can forget about direct socket programming, and leverage a ready-made library that exposes a higher level interface. Now, you will simply have to implement this higher level interface.<br>

In this specific project we provide the full sorce code that makes up the <b>Lightstreamer Java Remote Adapter API</b> library.

## Compatibility

The library is compatible with Adapter Remoting Infrastructure since Server version 7.0.

## Using the API

Since the API is available from the Maven Central Repository, to setup your development environment add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.lightstreamer</groupId>
    <artifactId>ls-adapter-remote</artifactId>
    <version>1.4.1</version>
</dependency>
```

### LiteralBasedProvider Metadata Adapter

This project includes a simple full implementation of Remote Metadata Adapter in Java made available as sample for inspiration and/or extension.

The [LiteralBasedProvider](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-remote/tree/master/src/main/java/com/lightstreamer/adapters/remote/metadata) is the Remote equivalent of the *LiteralBasedProvider* Metadata Adapter in [Lightstreamer Java In-Process Adapter SDK](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#literalbasedprovider-metadata-adapter).
It extends the [com.lightstreamer.adapters.remote.MetadataProviderAdapter](https://lightstreamer.com/api/ls-adapter-remote/latest/com/lightstreamer/adapters/remote/MetadataProviderAdapter.html) abstract class (which in turn implements the [com.lightstreamer.adapters.remote.MetadataProvider](https://lightstreamer.com/api/ls-adapter-remote/latest/com/lightstreamer/adapters/remote/MetadataProvider.html) interface).
It is used in Lightstreamer examples and demos based on the Java Remote Adapter SDK, in combination with suitable Data Adapters and Clients.

The LiteralBasedProvider can be configured through suitable initialization parameters. See the [class documentation](https://lightstreamer.com/api/ls-adapter-remote/latest/com/lightstreamer/adapters/remote/metadata/LiteralBasedProvider.html) for details.

## Build

To build your own version of the Java Remote Adapter library, you have two options:
either use [Maven](https://maven.apache.org/) (or other build tools) to take care of dependencies and building (recommended) or gather the necessary jars yourself and build it manually.
For the sake of simplicity only the Maven case is detailed here.

### Maven
You can easily build this library using Maven through the pom.xml file located in the root folder of this project. As an alternative, you can use an alternative build tool (e.g. Gradle, Ivy, etc.) by converting the provided pom.xml file.

Assuming Maven is installed and available in your path you can build the library by running

```sh
 mvn package
```


## External Links

- [Maven repository](https://mvnrepository.com/artifact/com.lightstreamer/ls-adapter-remote/)

- [Examples](https://demos.lightstreamer.com/?p=lightstreamer&t=adapter&sadapterjava=remote)

- [API Reference](https://www.lightstreamer.com/api/ls-adapter-remote/latest/)

- [Adapter Remoting Infrastructure Network Protocol Specification](https://lightstreamer.com/api/ls-generic-adapter/latest/ARI%20Protocol.pdf)

## Support

For questions and support please use the [Official Forum](https://forums.lightstreamer.com/). The issue list of this page is **exclusively** for bug reports and feature requests.

## License

[Apache 2.0](https://opensource.org/licenses/Apache-2.0)
