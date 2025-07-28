package dev.yoon.study_springboot_websocket_stomp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatMessage {
    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    private MessageType type;
    private String roomId;
    private String sender;
    private String message;
    private long userCount; // 현재 채팅방 사용자 수
    private List<String> users; // 현재 채팅방 사용자 목록
}