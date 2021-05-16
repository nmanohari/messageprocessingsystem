package com.echoworx.rules;

import com.echoworx.dto.Message;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ReversalRule implements Rule {
    @Override
    public Message apply(Message message) {
        message.setBody(message.getBody().stream().map(l -> reverseWords(l)).collect(Collectors.toList()));
        return message;
    }

    private static String reverseWords(String line) {
        StringBuffer br = new StringBuffer();
        String[] words = line.split(" ");
        for (int i = 0; i < words.length; i++) {
            char[] wr = words[i].toCharArray();
            int begin = 0;
            int last = wr.length - 1;
            char temp;
            while (begin < last) {
                temp = wr[begin];
                wr[begin] = wr[last];
                wr[last] = temp;
                begin++;
                last--;

            }
            br.append(wr).append(" ");
        }
        return br.toString();
    }
}
