package org.emailservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class MailObject {
    private String name;
    private String email;
    private String message;
}
