package com.mabl.integration.jenkins.test.output;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.Collection;

@XStreamAlias("testcase")
public class TestCase {

    @XStreamAsAttribute
    @XStreamAlias("classname")
    private String plan;

    @XStreamAsAttribute
    @XStreamAlias("name")
    private String journey;

    @XStreamAsAttribute
    @XStreamAlias("time")
    private long duration;

    @XStreamAsAttribute
    @XStreamAlias("xlink:type")
    private String linkType;

    @XStreamAsAttribute
    @XStreamAlias("xlink:href")
    private String appHref;

    private Failure failure;

    @XStreamAsAttribute
    private Skipped skipped;

    // Note that this is nan-standard element.
    // XRay supports this extension, see
    // https://docs.getxray.app/display/XRAYCLOUD/Taking+advantage+of+JUnit+XML+reports
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
        if (properties == null) {
            properties = new Properties();
        }
        properties.addProperty("requirement", String.join(",", testCaseIDs));
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


