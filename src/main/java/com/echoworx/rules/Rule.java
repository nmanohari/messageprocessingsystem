package com.echoworx.rules;

import com.echoworx.dto.Message;

public interface Rule {
    Message apply(Message body);
}
