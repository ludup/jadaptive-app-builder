package com.jadaptive.api.app.certificates;

import java.security.Key;
import java.security.cert.X509Certificate;

public record KeyHolder(KeyType type, String alias, Key key, X509Certificate... chain) {
}
