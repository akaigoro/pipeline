package com.github.rfqu.json.pushparser;

public class StringSource extends CharSource {
    final String line;
    final int length;
    int pos = 0;

    public StringSource(String line) {
        this.line = line;
        this.length = line.length();
    }

    @Override
    protected int nextChar() {
        if (pos==length) {
            pos++;
            return Scanner.NEWL;
        } else if (pos>length) {
            return -1;
        }
        return line.charAt(pos++);
    }
    
}