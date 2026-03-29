package com.jadaptive.api.app.messaging;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.jadaptive.api.user.User;

@Service
public class MessageBus {
    private final Map<String, MessageSubscriber> registry = new ConcurrentHashMap<>();

    public void register(String sourceId, MessageSubscriber sub) {
        registry.put(sourceId, sub);
    }

    public void dispatch(String sourceId, User user, String platform, Map<String, Object> metadata, String payload) {
        Optional.ofNullable(registry.get(sourceId))
                .ifPresent(sub -> sub.onMessage(sourceId, user, platform, metadata, payload));
    }
}