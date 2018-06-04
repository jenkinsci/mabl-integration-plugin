package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "property")
public class Property {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "value")
    private String value;

    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Property() {

    }
}
