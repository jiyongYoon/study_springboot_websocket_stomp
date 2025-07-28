package dev.yoon.study_springboot_websocket_stomp.controller;

import dev.yoon.study_springboot_websocket_stomp.dto.ChatMessage;
import dev.yoon.study_springboot_websocket_stomp.listener.WebSocketEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private WebSocketEventListener webSocketEventListener;

    // 클라이언트에서 /app/chat.sendMessage 로 메시지를 보내면 이 메서드가 처리합니다.
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // 메시지를 보낼 때, 해당 채팅방의 사용자 수와 목록을 업데이트하여 함께 전송합니다.
        chatMessage.setUserCount(webSocketEventListener.getUserCount(chatMessage.getRoomId()));
        chatMessage.setUsers(webSocketEventListener.getUsersInRoom(chatMessage.getRoomId())); // 사용자 목록 추가
        // 메시지를 해당 채팅방의 모든 구독자에게 전송합니다.
        webSocketEventListener.messagingTemplate.convertAndSend("/topic/public/" + chatMessage.getRoomId(), chatMessage);
    }

    // 클라이언트에서 /app/chat.addUser 로 메시지를 보내면 이 메서드가 처리합니다.
    // 사용자가 채팅방에 입장할 때 호출됩니다.
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // WebSocket 세션에 사용자 이름과 방 ID를 저장합니다.
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("roomId", chatMessage.getRoomId());

        // WebSocketEventListener를 통해 채팅방에 사용자를 추가합니다.
        webSocketEventListener.addUserToRoom(chatMessage.getRoomId(), chatMessage.getSender());

        // 입장 메시지를 생성하고 사용자 목록을 업데이트하여 함께 전송합니다.
        chatMessage.setType(ChatMessage.MessageType.ENTER);
        chatMessage.setMessage(chatMessage.getSender() + " entered the chat.");
        chatMessage.setUserCount(webSocketEventListener.getUserCount(chatMessage.getRoomId()));
        chatMessage.setUsers(webSocketEventListener.getUsersInRoom(chatMessage.getRoomId())); // 사용자 목록 추가

        // 입장 메시지를 해당 채팅방의 모든 구독자에게 전송합니다.
        webSocketEventListener.messagingTemplate.convertAndSend("/topic/public/" + chatMessage.getRoomId(), chatMessage);
    }
}