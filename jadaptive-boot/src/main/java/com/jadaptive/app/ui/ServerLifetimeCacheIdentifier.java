package com.jadaptive.app.ui;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class ServerLifetimeCacheIdentifier {

    private final String etag;

    public ServerLifetimeCacheIdentifier() {
        // The quotes are a required part of the ETag format.
        this.etag = "\"" + UUID.randomUUID().toString() + "\"";
    }

    public String getEtag() {
        return etag;
    }
}