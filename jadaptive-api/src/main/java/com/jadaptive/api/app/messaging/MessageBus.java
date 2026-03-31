package com.jadaptive.api.app.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jadaptive.api.user.User;

import jakarta.activation.MailcapCommandMap;

@Service
public class MessageBus {
    
	
	private final Collection<MessageSubscriber> registry = new ArrayList<>();

	private final Map<String,List<MessageSubscriber>> subscriberMap = new HashMap<>();
    
	public void register(MessageSubscriber sub) {
		/**
		 * Register a MessageSubscriber to receive messages from the MessageBus. This method adds the subscriber to a central 
		 * registry, allowing it to receive messages that are broadcasted to all subscribers. Subscribers can also be 
		 * registered to specific sourceIds for more targeted message delivery using the subscribe method.
		 */
        registry.add(sub);
    }
	
	public void subscribe(String sourceId, MessageSubscriber sub) {
		/**
		 * Subscribe a MessageSubscriber to a specific sourceId. This allows for targeted message delivery, where 
		 * only subscribers interested in a particular sourceId will receive messages from that source. If there 
		 * are no subscribers for a given sourceId, messages can be broadcasted to all subscribers in the registry.
		 */
		subscriberMap.computeIfAbsent(sourceId, k -> new ArrayList<>()).add(sub);
	}

    public void dispatch(Message m) {
    	if(!m.broadcast() && subscriberMap.containsKey(m.sourceId())) {
    		subscriberMap.get(m.sourceId()).forEach(s -> s.onMessage(m));
    	} else {
    		/**
    		 * Broadcast to all subscribers if there are no specific subscribers for the sourceId. This allows for a 
    		 * more flexible messaging system where some messages can be handled by specific subscribers while others 
    		 * can be broadcasted to all interested parties.
    		 * 
    		 * If this is non-broadcast message it is still send to all subscribers, but the expectation is that only 
    		 * those that are subscribed to the sourceId will process it and then subscribe for future messages from that sourceId.
    		 * This allows for a more dynamic subscription model where subscribers can choose to subscribe to specific sources 
    		 * after receiving an initial message.
    		 * 
    		 */
    		registry.stream().forEach(s -> s.onMessage(m));
    	}
    }
}