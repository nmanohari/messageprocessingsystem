package com.echoworx.controller;

import com.echoworx.dto.Message;
import com.echoworx.service.MessageProcessingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Log4j2
@RestController
public class MessageController {

    @Autowired
    private MessageProcessingService messageProcessingService;

    @RequestMapping("/")
    public ResponseEntity<String> sendMessage(@RequestParam String filePath) {
        try {
            Message message = messageProcessingService.readMessageFromFile(filePath);
            print(message);
            print("**********");
            message = messageProcessingService.findAndApplyRules(message);
            messageProcessingService.generateOutPut(filePath, message);
            print(message);
            return ResponseEntity.ok().body("SUCCESS");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            log.error(exceptionAsString);
            return ResponseEntity.ok().body("FAILED");
        }
    }

    private static void print(Message message) {
        print(message.getTo());
        print(message.getFrom());
        print(message.getSubject());
        print(message.getBody());
    }

    private static void print(String message) {
        log.info(message);
    }

    private static void print(List<String> msgList) {
        msgList.forEach(MessageController::print);
    }
}
