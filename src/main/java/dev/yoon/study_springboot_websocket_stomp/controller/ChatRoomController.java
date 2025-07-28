package dev.yoon.study_springboot_websocket_stomp.controller;

import dev.yoon.study_springboot_websocket_stomp.dto.ChatRoom;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private List<ChatRoom> chatRooms = new ArrayList<>();

    // 채팅방 목록 페이지를 반환합니다.
    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("chatRooms", chatRooms);
        return "chat/rooms"; // src/main/resources/templates/chat/rooms.html
    }

    // 새로운 채팅방을 생성합니다.
    @PostMapping("/rooms")
    public String createRoom(@RequestParam String roomName) {
        ChatRoom chatRoom = ChatRoom.create(roomName);
        chatRooms.add(chatRoom);
        return "redirect:/chat/rooms";
    }

    // 특정 채팅방 페이지를 반환합니다.
    @GetMapping("/rooms/{roomId}")
    public String chatRoom(@PathVariable String roomId, Model model) {
        // roomId를 사용하여 해당 채팅방을 찾습니다.
        Optional<ChatRoom> foundRoom = chatRooms.stream()
                .filter(chatRoom -> chatRoom.getRoomId().equals(roomId))
                .findFirst();

        if (foundRoom.isPresent()) {
            model.addAttribute("chatRoom", foundRoom.get());
            return "chat/room"; // src/main/resources/templates/chat/room.html
        } else {
            // 채팅방을 찾을 수 없는 경우, 에러 페이지 또는 목록 페이지로 리다이렉트합니다.
            return "redirect:/chat/rooms";
        }
    }
}
