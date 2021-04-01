package com.mabl.integration.jenkins.test.output;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("failure")
@XStreamConverter(value=ToAttributedValueConverter.class, strings={"reason"})
public class Failure {

    private String reason;

    @XStreamAsAttribute
    private String message;

    public Failure() {

    }

    public Failure(String reason, String message) {
        this.reason = reason;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getReason() {
        return this.reason;
    }
}
