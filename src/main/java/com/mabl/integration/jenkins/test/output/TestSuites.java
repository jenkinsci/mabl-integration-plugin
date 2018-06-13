package com.mabl.integration.jenkins.test.output;

import com.google.common.collect.ImmutableCollection;
import com.mabl.integration.jenkins.MablStepConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "testsuites")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestSuites {

    @XmlAttribute(name = "xmlns:xlink")
    private String xlink;

    @XmlElement(name = "testsuite")
    private ImmutableCollection<TestSuite> testSuites;

    public TestSuites(ImmutableCollection<TestSuite> testSuites) {
        this.testSuites = testSuites;
        this.xlink = MablStepConstants.TEST_OUTPUT_XML_XLINK;
    }

    public TestSuites() {

    }
}
