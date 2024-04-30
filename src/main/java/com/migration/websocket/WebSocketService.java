package com.migration.websocket;

import com.migration.dto.MigrationStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessageToClient(MigrationStatistics statistics) {
        messagingTemplate.convertAndSend("/topic/progress", statistics);
    }

    public void sendMessageToClient(String statistics) {
        messagingTemplate.convertAndSend("/topic/logs", statistics);
    }
}
