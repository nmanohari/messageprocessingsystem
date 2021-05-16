package com.echoworx.service;

import com.echoworx.dto.Message;
import com.echoworx.exceptions.MessageException;
import com.echoworx.rules.ReplacementRule;
import com.echoworx.rules.ReversalRule;
import com.echoworx.rules.Rule;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
@Log4j2
public class MessageProcessingService {
    public static final String TO = "To:";
    public static final String FROM = "From:";
    public static final String SUBJECT = "Subject:";
    public static final String BODY = "Body:";

    private Map<String, Rule> ruleMap;

    @Autowired
    private Rule reversalRule;

    @Autowired
    private Rule replacementRule;

    @Value("#{'${domain.names}'.split(',')}")
    private List<String> domainNames;

    @Value("#{'${subject.starts}'.split(',')}")
    private List<String> subjectStarts;

    @PostConstruct
    public void init() {
        ruleMap = new HashMap<>();
        ruleMap.put(ReplacementRule.class.getName(), replacementRule);
        ruleMap.put(ReversalRule.class.getName(), reversalRule);
    }

    public Message processMessage(String filePath) throws MessageException {
        Message message = readMessageFromFile(filePath);
        return findAndApplyRules(message);
    }

    public Message findAndApplyRules(Message message) {
        Set<Rule> rulesToApply = findRulesToApply(message);

        rulesToApply.forEach(rule -> rule.apply(message));
        return message;
    }

    private Set<Rule> findRulesToApply(Message message) {
        Set<Rule> rules = new LinkedHashSet<>();

        if (checkMessageDomains(message, domainNames)) {
            rules.add(ruleMap.get(ReplacementRule.class.getName()));
        }

        if (checkMessageSubject(message, subjectStarts)) {
            rules.add(ruleMap.get(ReversalRule.class.getName()));
        }

        if (checkMessageBody(message)) {
            rules.add(ruleMap.get(ReplacementRule.class.getName()));
            rules.add(ruleMap.get(ReversalRule.class.getName()));
        }
        return rules;
    }

    private boolean checkMessageBody(Message message) {
        return message.getBody().stream().anyMatch(line -> line.matches(".*\\d{10}.*"));
    }

    private boolean checkMessageSubject(Message message, List<String> subjects) {
        return subjects.stream().anyMatch(subject -> message.getSubject().startsWith(subject));
    }

    private boolean checkMessageDomains(Message message, List<String> domains) {
        return domains.stream().anyMatch(domain -> message.getTo().stream().anyMatch(to -> to.endsWith(domain)));
    }

    public Message readMessageFromFile(String filePath) throws MessageException {
        Message message = new Message();
        //List<String> fileContents = Files.readAllLines(Path.of(filePath));
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.forEach(l -> {
                if (l.toUpperCase(Locale.ENGLISH).startsWith(TO.toUpperCase(Locale.ENGLISH))) {
                    l = l.substring(TO.length());
                    message.setTo(Arrays.asList(l.split(",")));
                } else if (l.toUpperCase(Locale.ENGLISH).startsWith(FROM.toUpperCase(Locale.ENGLISH))) {
                    l = l.substring(FROM.length());
                    message.setFrom(l);
                } else if (l.toUpperCase(Locale.ENGLISH).startsWith(SUBJECT.toUpperCase(Locale.ENGLISH))) {
                    l = l.substring(SUBJECT.length());
                    message.setSubject(l);
                } else if (l.toUpperCase(Locale.ENGLISH).startsWith(BODY.toUpperCase(Locale.ENGLISH))) {
                    l = l.substring(BODY.length());
                    message.getBody().add(l);
                } else {
                    message.getBody().add(l);
                }
            });
            return message;
        } catch (IOException e) {
            throw new MessageException("Exception occured while reading the file.", e);
        }
    }

    public void generateOutPut(String filePath, Message message) throws MessageException {
        Path path = Paths.get(filePath.substring(0, filePath.lastIndexOf(".")) + "-output" + filePath.substring(filePath.lastIndexOf("."), filePath.length()));

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(TO + String.join(",", message.getTo()));
            writer.write("\n");
            writer.write(FROM + message.getFrom());
            writer.write("\n");
            writer.write(SUBJECT + message.getSubject());
            writer.write("\n");
            writer.write(BODY + String.join("\n", message.getBody()));
        } catch (IOException e) {
            throw new MessageException("Exception occured while writing the message to file.", e);
        }
    }

}
