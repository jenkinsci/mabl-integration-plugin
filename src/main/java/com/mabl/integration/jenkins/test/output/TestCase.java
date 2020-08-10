package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement(name = "testcase")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestCase {

    @XmlAttribute(name = "classname")
    private String plan;
    @XmlAttribute(name = "name")
    private String journey;
    @XmlAttribute(name = "time")
    private long duration;
    @XmlAttribute(name = "xlink:type")
    private String linkType;
    @XmlAttribute(name = "xlink:href")
    private String appHref;

    @XmlElement(name = "failure")
    private Failure failure;

    @XmlElement(name = "skipped")
    private Skipped skipped;

    // Note that this is nan-standard element.
    // XRay supports this extension, see
    // https://docs.getxray.app/display/XRAYCLOUD/Taking+advantage+of+JUnit+XML+reports
    @XmlElement(name = "properties")
    private Properties properties;

    public TestCase() {

    }

    public TestCase(String plan, String journey, long duration, String appHref) {
        this(plan, journey, duration, appHref, null);
    }

    public TestCase(String plan, String journey, long duration, String appHref, Failure failure) {
        this.plan = plan;
        this.journey = journey;
        this.duration = duration;
        this.linkType = "simple";
        this.appHref = appHref;
        this.failure = failure;
    }

    public void setTestCaseIDs(final Collection<String> testCaseIDs) {
        final List<Property> props = new ArrayList<>();
        props.add(new Property("requirement", String.join(",", testCaseIDs)));
        if (properties == null) {
            properties = new Properties(props);
        } else {
            properties.addProperties(props);
        }
    }

    public void setFailure(Failure failure) {
        this.failure = failure;
    }

    public void setSkipped() {
        this.skipped = new Skipped();
    }

    public String getPlan() {
        return this.plan;
    }

    public String getJourney() {
        return this.journey;
    }

    public long getDuration() {
        return this.duration;
    }

    public String getAppHref() {
        return this.appHref;
    }

    public String getLinkType() {
        return this.linkType;
    }

    public Failure getFailure() {
        return this.failure;
    }

    public Properties getProperties() { return this.properties; }

    public Skipped getSkipped() { return this.skipped; }
}


