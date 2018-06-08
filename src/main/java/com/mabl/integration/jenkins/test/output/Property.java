package com.mabl.integration.jenkins.test.output;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
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

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }
}
