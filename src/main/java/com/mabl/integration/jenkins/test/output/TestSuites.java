package com.mabl.integration.jenkins.test.output;

import com.google.common.collect.ImmutableCollection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "testsuites")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestSuites {

    @XmlElement(name = "testsuite")
    private ImmutableCollection<TestSuite> testSuites;

    public TestSuites(ImmutableCollection<TestSuite> testSuites) {
        this.testSuites = testSuites;
    }

    public TestSuites() {

    }
}
