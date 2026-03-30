package com.example.badrock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessage {
    private String username;
    private String messageText;
    private String followUpQuestion;

    public ChatMessage(String messageText, String username) {
        this.messageText = messageText;
        this.username = username;
    }
}