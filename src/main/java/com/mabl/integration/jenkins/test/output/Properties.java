package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

@XmlRootElement(name = "properties")
@XmlAccessorType(XmlAccessType.FIELD)
public class Properties {

    @XmlElement(name = "property")
    private Collection<Property> properties;

    public Properties(Collection<Property> properties) {
        this.properties = properties;
    }

    public Properties() {

    }

    public Collection<Property> getProperties() {
        return this.properties;
    }
}
