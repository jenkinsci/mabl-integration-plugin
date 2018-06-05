package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "testcase")
public class TestCase {

    @XmlAttribute(name = "classname")
    private String plan;
    @XmlAttribute(name = "name")
    private String journey;
    @XmlAttribute(name = "time")
    private long duration;

    @XmlElement(name = "failure")
    private Failure failure;

    public TestCase() {

    }

    public TestCase(String plan, String journey, long duration) {
        this.plan = plan;
        this.journey = journey;
        this.duration = duration;
    }

    public TestCase(String plan, String journey, long duration, Failure failure) {
        this.plan = plan;
        this.journey = journey;
        this.duration = duration;
        this.failure = failure;
    }

    public TestCase setFailure(Failure failure) {
        this.failure = failure;
        return this;
    }
}


