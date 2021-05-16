package com.echoworx.rules;

import com.echoworx.dto.Message;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ReplacementRule implements Rule {

    @Override
    public Message apply(Message message) {
        message.setBody(message.getBody().stream().map(line ->
                line.replaceAll("\\$", "e")
                        .replaceAll("\\^", "y")
                        .replaceAll("\\&", "u")
        ).collect(Collectors.toList()));

        return message;
    }
}
