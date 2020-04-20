# Lightstreamer Java Remote Adapter SDK

This project includes the resources needed to develop Remote Data Adapters and Remote Metadata Adapters for [Lightstreamer Server](http://www.lightstreamer.com/) in Java.
Java 8 or higher is supported.
The adapters will run in a separate process, communicating with the Server through the Proxy Adapters.

![architecture](architecture.png)

## Compatibility ##

The library is compatible with [Adapter Remoting Infrastructure](https://lightstreamer.com/docs/adapter_generic_base/ARI%20Protocol.pdf) since version 1.8.

## Include the library in your projects ##

The library jar is deployed on a Maven repository at https://www.lightstreamer.com/repo/maven

To include the library in a custom project, using any maven-compatible build tool (e.g. Maven, Gradle, Ivy, etc.) it is necessary to configure both a
pointer to the external repository and the reference to the lib itself.

Please note that the examples below are based on the library version currently available. 

### Maven Example ###

<repositories>
  <repository>
    <id>lightstreamer</id>
    <name>Lightstreamer repository</name>
    <url>https://www.lightstreamer.com/repo/maven</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.lightstreamer</groupId>
    <artifactId>ls-adapter-remote</artifactId>
    <version>1.4.0</version>
  </dependency>
</dependencies>

### Gradle Example ###

repositories {
    maven {
        url "https://www.lightstreamer.com/repo/maven"
    }
}

dependencies {
    compile 'com.lightstreamer:ls-adapter-remote:1.4.0'
}

### Ivy Example ###

<ibiblio name="lightstreamer" m2compatible="true" root="https://www.lightstreamer.com/repo/maven/"/>

<dependency org="com.lightstreamer" name="ls-adapter-remotet" rev="1.4.0"/>

## Documentation

- [Maven repository](https://www.lightstreamer.com/repo/maven/)

- [Live demos](https://demos.lightstreamer.com/?p=lightstreamer&t=adapter&sadapterjava=remote)

- [API Reference](https://lightstreamer.com/docs/adapter_java_remote_api/index.html)

## Other GitHub projects using this library

- [Lightstreamer-example-StockList-client-dotnet ](https://github.com/Lightstreamer/Lightstreamer-example-StockList-client-dotnet)

## Support

For questions and support please use the [Official Forum](https://forums.lightstreamer.com/). The issue list of this page is **exclusively** for bug reports and feature requests.

## License

[Apache 2.0](https://opensource.org/licenses/Apache-2.0)
