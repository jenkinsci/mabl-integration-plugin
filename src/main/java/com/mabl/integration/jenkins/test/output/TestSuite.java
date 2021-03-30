package com.mabl.integration.jenkins.test.output;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XStreamAlias("testsuite")
public class TestSuite {

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private int tests;

    @XStreamAsAttribute
    private int errors;

    @XStreamAsAttribute
    private int failures;

    @XStreamAsAttribute
    private int skipped;

    @XStreamAsAttribute
    private long time;

    @XStreamAsAttribute
    private String timestamp;

    @XStreamAlias("properties")
    private List<Property> properties;

    @XStreamImplicit
    private List<TestCase> testCase;

    public TestSuite(String name, long time, String timestamp, Properties properties) {
        this.name = name;
        this.time = time;
        this.timestamp = timestamp;
        this.properties = properties != null && properties.getProperties() != null ? new ArrayList<>(properties.getProperties()) : null;
        this.testCase = new ArrayList<>();
    }

    public TestSuite(String name, long time, String timestamp) {
        this.name = name;
        this.time = time;
        this.timestamp = timestamp;
        this.testCase = new ArrayList<>();
    }

    public TestSuite() {

    }

    public TestSuite addToTestCases(TestCase testCase) {
        this.testCase.add(testCase);
        return this;
    }

    public void incrementTests() {
        this.tests++;
    }

    public void incrementErrors() {
        this.errors++;
    }

    public void incrementFailures() {
        this.failures++;
    }

    public void incrementSkipped() {
        this.skipped++;
    }

    public void addProperty(final String name, final String value) {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        properties.add(new Property(name, value));
    }

    public String getName() {
        return this.name;
    }

    public int getTests() {
        return this.tests;
    }

    public int getErrors() {
        return this.errors;
    }

    public int getFailures() {
        return this.failures;
    }

    public int getSkipped() { return this.skipped; }

    public long getTime() {
        return this.time;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public List<TestCase> getTestCases() { return Collections.unmodifiableList(testCase); }

    public Properties getProperties() {
        return new Properties(properties);
    }

}
