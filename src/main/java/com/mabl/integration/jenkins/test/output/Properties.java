package com.mabl.integration.jenkins.test.output;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("properties")
public class Properties {

    @XStreamAlias("property")
    private List<Property> properties = new ArrayList<>();

    public Properties(List<Property> properties) {
        if (properties != null && !properties.isEmpty()) {
            this.properties.addAll(properties);
        }
    }

    public Properties() {

    }

    public ImmutableCollection<Property> getProperties() {
        return new ImmutableList.Builder<Property>().addAll(properties).build();
    }

    public void addProperty(String name, String value) {
        properties.add(new Property(name, value));
    }

}
