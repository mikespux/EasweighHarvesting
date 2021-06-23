package com.plantation.fingerprintreader;

@SuppressWarnings("serial")
public class Dict implements Comparable<Dict> {
    private String id;
    private String text;

    public Dict() {
    }

    public Dict(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public int compareTo(Dict another) {
        return Integer.parseInt(this.getId()) - (Integer.parseInt(another.getId()));
    }
}
