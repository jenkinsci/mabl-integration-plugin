package com.mabl.integration.jenkins.test.output;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("property")
public class Property {

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
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
