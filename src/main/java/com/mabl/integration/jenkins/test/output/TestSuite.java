package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement(name="testsuite")
public class TestSuite {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "tests")
    private int tests;

    @XmlAttribute(name = "errors")
    private int errors;

    @XmlAttribute(name = "failures")
    private int failures;

    @XmlAttribute(name = "time")
    private long time;

    @XmlAttribute(name = "timestamp")
    private String timestamp;

    @XmlElement(name = "properties")
    private Properties properties;

    @XmlElement(name = "testcase")
    private Collection<TestCase> testCases;

    public TestSuite(String name, long time, String timestamp, Properties properties) {
        this.name = name;
        this.time = time;
        this.timestamp = timestamp;
        this.properties = properties;
        this.testCases = new ArrayList<TestCase>();
    }

    public TestSuite() {

    }

    public Collection<TestCase> addToTestCases(TestCase testCase) {
        this.testCases.add(testCase);
        return this.testCases;
    }

    public int incrementTests() {
        this.tests++;
        return this.tests;
    }

    public int incrementErrors() {
        this.errors++;
        return this.errors;
    }

    public int incrementFailures() {
        this.failures++;
        return this.failures;
    }

}
