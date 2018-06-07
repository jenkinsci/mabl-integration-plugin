package com.mabl.integration.jenkins.test.output;

import com.google.common.collect.ImmutableCollection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "properties")
@XmlAccessorType(XmlAccessType.FIELD)
public class Properties {

    @XmlElement(name = "property")
    private ImmutableCollection<Property> properties;

    public Properties(ImmutableCollection<Property> properties) {
        this.properties = properties;
    }

    public Properties() {

    }

    public ImmutableCollection<Property> getProperties() {
        return this.properties;
    }
}
