package com.echoworx.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Message {
    public Message() {
        body = new ArrayList<>();
    }

    private List<String> to;
    private String from;
    private String subject;
    private List<String> body;
}
