package dev.yoon.study_springboot_websocket_stomp.listener;

import dev.yoon.study_springboot_websocket_stomp.dto.ChatMessage;
import dev.yoon.study_springboot_websocket_stomp.dto.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    @Autowired
    public SimpMessageSendingOperations messagingTemplate; // public으로 변경하여 ChatController에서 접근 가능하도록 함

    // 채팅방별 사용자 목록을 관리합니다.
    private Map<String, ChatRoom> activeChatRooms = new ConcurrentHashMap<>();

    // WebSocket 연결 시 이벤트 리스너
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // 연결 시 특별한 동작은 하지 않습니다.
        System.out.println("Received a new web socket connection");
    }

    // WebSocket 연결 해제 시 이벤트 리스너
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");

        if (username != null && roomId != null) {
            System.out.println("User Disconnected: " + username + " from room: " + roomId);

            ChatRoom chatRoom = activeChatRooms.get(roomId);
            if (chatRoom != null) {
                chatRoom.getUsers().remove(username);

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(ChatMessage.MessageType.LEAVE);
                chatMessage.setRoomId(roomId);
                chatMessage.setSender(username);
                chatMessage.setMessage(username + " left the chat.");
                chatMessage.setUserCount(chatRoom.getUsers().size());
                chatMessage.setUsers(new ArrayList<>(chatRoom.getUsers())); // 사용자 목록 업데이트

                // 사용자 목록과 함께 퇴장 메시지를 해당 채팅방에 브로드캐스트합니다.
                messagingTemplate.convertAndSend("/topic/public/" + roomId, chatMessage);

                // 만약 채팅방에 아무도 없으면 채팅방을 목록에서 제거합니다.
                if (chatRoom.getUsers().isEmpty()) {
                    activeChatRooms.remove(roomId);
                }
            }
        }
    }

    // 사용자가 채팅방에 입장할 때 호출되는 메서드 (ChatController에서 호출)
    public void addUserToRoom(String roomId, String username) {
        activeChatRooms.computeIfAbsent(roomId, k -> {
            ChatRoom newRoom = new ChatRoom();
            newRoom.setRoomId(roomId);
            return newRoom;
        }).getUsers().add(username);
    }

    // 특정 채팅방의 사용자 수를 반환합니다.
    public long getUserCount(String roomId) {
        ChatRoom chatRoom = activeChatRooms.get(roomId);
        return chatRoom != null ? chatRoom.getUsers().size() : 0;
    }

    // 특정 채팅방의 사용자 목록을 반환합니다.
    public List<String> getUsersInRoom(String roomId) {
        ChatRoom chatRoom = activeChatRooms.get(roomId);
        return chatRoom != null ? new ArrayList<>(chatRoom.getUsers()) : new ArrayList<>();
    }
}
