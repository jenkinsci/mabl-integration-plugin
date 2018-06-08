package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "failure")
@XmlAccessorType(XmlAccessType.FIELD)
public class Failure {

    @XmlValue()
    private String reason;

    @XmlAttribute(name = "message")
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
