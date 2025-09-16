package com.vinncorp.fast_learner.mock.core;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayTuple implements Tuple {
    private final Object[] elements;
    private final Map<String, Integer> aliasToIndexMap;

    public ArrayTuple(Object[] elements, String[] fieldNames) {
        this.elements = elements;
        this.aliasToIndexMap = buildAliasToIndexMap(fieldNames);
    }

    private Map<String, Integer> buildAliasToIndexMap(String[] fieldNames) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < fieldNames.length; i++) {
            String element = fieldNames[i];
            map.put(element, i);
        }
        return map;
    }

    @Override
    public <T> T get(int i, Class<T> aClass) {
        return elements[i].equals("null") ? null : aClass.cast(elements[i]);
    }

    @Override
    public Object get(int i) {
        return elements[i].equals("null") ? null : elements[i];
    }

    @Override
    public Object get(String s) {
        Integer index = aliasToIndexMap.get(s);
        if (index == null) {
            throw new IllegalArgumentException("Field name not found: " + s);
        }
        return elements[index].equals("null") ? null : elements[index];
    }

    @Override
    public <X> X get(TupleElement<X> tupleElement) {
        return null;
    }

    @Override
    public <T> T get(String s, Class<T> aClass) {
        Object value = get(s);
        if (value == null) {
            return null;
        }
        return aClass.cast(value);
    }

    @Override
    public Object[] toArray() {
        return elements;
    }

    @Override
    public List<TupleElement<?>> getElements() {
        throw new UnsupportedOperationException();
    }
}
