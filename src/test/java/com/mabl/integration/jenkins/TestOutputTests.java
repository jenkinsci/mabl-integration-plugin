package com.mabl.integration.jenkins;

import com.google.common.collect.ImmutableList;
import com.mabl.integration.jenkins.test.output.Failure;
import com.mabl.integration.jenkins.test.output.Properties;
import com.mabl.integration.jenkins.test.output.Property;
import com.mabl.integration.jenkins.test.output.TestCase;
import com.mabl.integration.jenkins.test.output.TestSuite;
import com.mabl.integration.jenkins.test.output.TestSuites;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestOutputTests {

    @Test
    public void testTestCaseOutputNoFailure() throws JAXBException {
        TestCase testCase = new TestCase("My Plan Name", "My Journey Name", 23L);
        JAXBContext jaxbContext = JAXBContext.newInstance(TestCase.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(testCase, stringWriter);
        assertEquals(MablTestConstants.TEST_CASE_XML, stringWriter.toString());
    }

    @Test
    public void testTestCaseOutputWithFailure() throws JAXBException {
        Failure failure = new Failure("My Reason", "My Message");
        TestCase testCase = new TestCase("My Plan Name", "My Journey Name", 23L, failure);
        JAXBContext jaxbContext = JAXBContext.newInstance(TestCase.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(testCase, stringWriter);
        assertEquals(MablTestConstants.TEST_CASE_XML_WITH_FAILURE, stringWriter.toString());
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

        TestCase testCase1 = new TestCase("My Plan Name 1", "My Journey Name 1", 11L);
        Failure failure = new Failure("My Reason", "My Message");
        TestCase testCase2 = new TestCase("My Plan Name 2", "My Journey Name 2", 22L, failure);
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

        JAXBContext jaxbContext = JAXBContext.newInstance(TestSuites.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(testSuites, stringWriter);
        assertEquals(MablTestConstants.TEST_SUITES_XML, stringWriter.toString());
    }
}
