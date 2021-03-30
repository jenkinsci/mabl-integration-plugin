package com.mabl.integration.jenkins;

import com.google.common.collect.ImmutableList;
import com.mabl.integration.jenkins.test.output.Failure;
import com.mabl.integration.jenkins.test.output.Properties;
import com.mabl.integration.jenkins.test.output.Property;
import com.mabl.integration.jenkins.test.output.Skipped;
import com.mabl.integration.jenkins.test.output.TestCase;
import com.mabl.integration.jenkins.test.output.TestSuite;
import com.mabl.integration.jenkins.test.output.TestSuites;
import com.thoughtworks.xstream.XStream;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestOutputTests {

    private XStream xstream;

    @Before
    public void configureXStream() {
        xstream = new XStream();
        xstream.processAnnotations(Failure.class);
        xstream.processAnnotations(Properties.class);
        xstream.processAnnotations(Property.class);
        xstream.processAnnotations(Skipped.class);
        xstream.processAnnotations(TestCase.class);
        xstream.processAnnotations(TestSuite.class);
        xstream.processAnnotations(TestSuites.class);
    }

    @Test
    public void testTestCaseOutputNoFailure() {
        TestCase testCase = new TestCase("My Plan Name", "My Test Name", 23L, "http://myapphref.com");
        String xml = xstream.toXML(testCase);
        assertEquals(MablTestConstants.TEST_CASE_XML, xml);
    }

    @Test
    public void testTestCaseOutputWithFailure() throws JAXBException {
        Failure failure = new Failure("My Reason", "My Message");
        TestCase testCase = new TestCase("My Plan Name", "My Test Name", 23L, "http://myapphref.com", failure);

        String xml = xstream.toXML(testCase);

        assertEquals(MablTestConstants.TEST_CASE_XML_WITH_FAILURE, xml);
    }

    @Test
    public void testEntireTestSuite() throws JAXBException {
        TestSuite emptyTestSuite = new TestSuite(
                "Empty Test Suite",
                0L,
                "2013-05-24T10:23:58",
                new Properties()
        );

        Property property1 = new Property("environment", "my env-e");
        Property property2 = new Property("application", "my app-a");
        ArrayList<Property> props = new ArrayList<Property>();
        props.add(property1);
        props.add(property2);
        Properties properties = new Properties(ImmutableList.copyOf(props));

        TestCase testCase1 = new TestCase("My Plan Name 1", "My Test Name 1", 11L, "http://myapphref.com");
        Failure failure = new Failure("My Reason", "My Message");
        TestCase testCase2 = new TestCase("My Plan Name 2", "My Test Name 2", 22L, "http://myapphref.com", failure);
        TestSuite testSuite1 = new TestSuite(
                "Full Test Suite",
                33L,
                "2013-05-24T10:23:58",
                properties
        );
        testSuite1.addToTestCases(testCase1);
        testSuite1.incrementTests();
        testSuite1.addToTestCases(testCase2);
        testSuite1.incrementTests();
        testSuite1.incrementFailures();

        ArrayList<TestSuite> suites = new ArrayList<TestSuite>();
        suites.add(emptyTestSuite);
        suites.add(testSuite1);
        TestSuites testSuites = new TestSuites(ImmutableList.copyOf(suites));

        String xml = xstream.toXML(testSuites);
        assertEquals(MablTestConstants.TEST_SUITES_XML, xml);
    }
}
