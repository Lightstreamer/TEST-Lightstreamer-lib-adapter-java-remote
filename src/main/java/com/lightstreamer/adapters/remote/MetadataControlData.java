package com.lightstreamer.adapters.remote;

import java.util.concurrent.CompletableFuture;

class MetadataControlData {

    public String requestID;
    public String request;
    public CompletableFuture<?> future;

}
