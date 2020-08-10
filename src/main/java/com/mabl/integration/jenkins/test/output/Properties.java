package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
        return Collections.unmodifiableCollection(this.properties);
    }

    public void addProperty(String name, String value) {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        properties.add(new Property(name, value));
    }

    public void addProperties(Collection<Property> props) {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        properties.addAll(props);
    }
}
