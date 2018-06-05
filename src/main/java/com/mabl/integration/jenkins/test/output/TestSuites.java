package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

@XmlRootElement(name = "testsuites")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestSuites {

    @XmlElement(name = "testsuite")
    private Collection<TestSuite> testSuites;

    public TestSuites(Collection<TestSuite> testSuites) {
        this.testSuites = testSuites;
    }

    public TestSuites() {

    }
}
