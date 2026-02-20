package com.dgphoenix.casino.gs.singlegames.tools.cbservtools.autofinish;


import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

public class ServletOutputStreamExt extends ServletOutputStream {

    private StringBuilder result = new StringBuilder();

    public ServletOutputStreamExt() {
    }

    @Override
    public void write(int i) throws IOException {
        result.append(i);
    }

    @Override
    public void print(String s) {
        result.append(s);
    }

    @Override
    public void print(boolean b) {
        result.append(b);
    }

    @Override
    public void print(char c) {
        result.append(c);
    }

    @Override
    public void print(int i) {
        result.append(i);
    }

    @Override
    public void print(long l) {
        result.append(l);
    }

    @Override
    public void print(float f) {
        result.append(f);
    }

    @Override
    public void print(double d) {
        result.append(d);
    }

    @Override
    public void println(String s) {
        result.append(s).append("\n");
    }

    @Override
    public void println(boolean b) {
        result.append(b).append("\n");
    }

    @Override
    public void println(char c) {
        result.append(c).append("\n");
    }

    @Override
    public void println(int i) {
        result.append(i).append("\n");
    }

    @Override
    public void println(long l) {
        result.append(l).append("\n");
    }

    @Override
    public void println(float f) {
        result.append(f).append("\n");
    }

    @Override
    public void println(double d) {
        result.append(d).append("\n");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    @Override
    public String toString() {
        return result.toString();
    }
}