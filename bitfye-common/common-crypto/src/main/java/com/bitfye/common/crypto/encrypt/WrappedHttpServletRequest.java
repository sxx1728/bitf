package com.bitfye.common.crypto.encrypt;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

class WrappedHttpServletRequest extends HttpServletRequestWrapper {
    static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    boolean flag = false;
    ByteArrayInputStream input;

    public WrappedHttpServletRequest(HttpServletRequest request, ByteArrayInputStream input) {
        super(request);
        this.input = input;
    }

    @Override
    public String getHeader(String name) {
        return name.equalsIgnoreCase("content-type") ? "application/json; charset=utf-8" : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return name.equalsIgnoreCase("content-type") ? Collections.enumeration(Arrays.asList("application/json; charset=utf-8")) : super.getHeaders(name);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.flag) {
            throw new IOException("Cannot reopen InputStream.");
        } else {
            this.flag = true;
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return WrappedHttpServletRequest.this.input.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                }

                @Override
                public int read() throws IOException {
                    return WrappedHttpServletRequest.this.input.read();
                }
            };
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (this.flag) {
            throw new IOException("Cannot reopen Reader.");
        } else {
            this.flag = true;
            return new BufferedReader(new InputStreamReader(this.input, "UTF-8"));
        }
    }
}

