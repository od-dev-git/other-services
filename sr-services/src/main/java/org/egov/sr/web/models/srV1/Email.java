package org.egov.sr.web.models.srV1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
public class Email {
    private String toAddress;
    private String subject;
    private String body;
    private boolean html;
}
