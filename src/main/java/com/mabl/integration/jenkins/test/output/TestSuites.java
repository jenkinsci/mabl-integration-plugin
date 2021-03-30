package com.mabl.integration.jenkins.test.output;

import com.google.common.collect.ImmutableCollection;
import com.mabl.integration.jenkins.MablStepConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("testsuites")

public class TestSuites {

    @XStreamAsAttribute
    @XStreamAlias("xmlns:xlink")
    private String xlink;

    @XStreamImplicit(itemFieldName = "testsuite")
    private ImmutableCollection<TestSuite> testSuite;

    public TestSuites(ImmutableCollection<TestSuite> testSuites) {
        this.testSuite = testSuites;
        this.xlink = MablStepConstants.TEST_OUTPUT_XML_XLINK;
    }

    public TestSuites() {

    }

    public String getXlink() {
        return xlink;
    }

    public ImmutableCollection<TestSuite> getTestSuites() {
        return testSuite;
    }
}
