package com.github.libgraviton.workerbase.gdk.api.header;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Header implements Iterable<String> {

    private final List<String> values;

    public Header() {
        this(new ArrayList<>());
    }

    public Header(List<String> values) {
        this.values = values;
    }

    public boolean contains(String value) {
        return values.contains(value);
    }

    public String get(int index) {
        return values.get(index);
    }

    public List<String> all() {
        return new ArrayList<>(values);
    }

    @Override
    public Iterator<String> iterator() {
        return values.iterator();
    }

}
