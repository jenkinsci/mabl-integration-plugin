package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

@XmlRootElement(name = "testsuites")
public class TestSuites {

    @XmlElement(name = "testsuite")
    private Collection<TestSuite> testSuites;

    public TestSuites(Collection<TestSuite> testSuites) {
        this.testSuites = testSuites;
    }

    public TestSuites() {

    }
}
