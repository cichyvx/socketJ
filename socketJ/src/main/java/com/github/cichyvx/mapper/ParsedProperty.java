package com.github.cichyvx.mapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ParsedProperty {

    private final List<String> value;
    private final List<Map<String, ParsedProperty>> nestedProperty;

    public ParsedProperty(String value) {
        this.value = new LinkedList<>();
        this.value.add(value);
        this.nestedProperty = null;
    }

    public ParsedProperty(Map<String, ParsedProperty> nestedProperty) {
        this.value = null;
        this.nestedProperty = new ArrayList<>(List.of(nestedProperty));
    }

    public boolean isNested() {
        return nestedProperty != null;
    }

    public List<String> getValue() {
        return value;
    }

    public List<Map<String, ParsedProperty>> getNestedProperty() {
        return nestedProperty;
    }

    public void append(String value) {
        this.value.add(value);
    }

    public void appendNested(Map<String, ParsedProperty> nestedProperty) {
        this.nestedProperty.add(nestedProperty);
    }

}
