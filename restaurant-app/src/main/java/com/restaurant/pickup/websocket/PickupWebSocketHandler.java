package com.restaurant.pickup.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PickupWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PickupWebSocketHandler.class);
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket connected: {} (total: {})", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket disconnected: {} (total: {})", session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // display screen only receives — no upstream handling needed
    }

    public void broadcast(PickupCallMessage message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            log.error("Failed to serialize pickup message", e);
            return;
        }
        sessions.removeIf(s -> !s.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                log.warn("Failed to send to session {}: {}", session.getId(), e.getMessage());
            }
        }
        log.info("Broadcasted pickup call to {} clients: {}", sessions.size(), message.method());
    }
}
