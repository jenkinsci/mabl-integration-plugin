package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "failure")
public class Failure {

    @XmlElement(name="reason")
    private String reason;

    @XmlAttribute(name = "message")
    private String message;

    public Failure() {

    }

    public Failure(String reason, String message) {
        this.reason = reason;
        this.message = message;
    }

}
