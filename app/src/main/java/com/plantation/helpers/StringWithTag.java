package com.plantation.helpers;

public class StringWithTag {
    public String string;
    public String tag;

    public StringWithTag(String stringPart, String tagPart) {
        string = stringPart;
        tag = tagPart;
    }

    @Override
    public String toString() {
        return string;
    }
}