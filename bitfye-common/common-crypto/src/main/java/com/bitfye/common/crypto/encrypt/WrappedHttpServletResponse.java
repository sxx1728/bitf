package com.bitfye.common.crypto.encrypt;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

class WrappedHttpServletResponse extends HttpServletResponseWrapper {
    boolean flag = false;
    ByteArrayOutputStream output = new ByteArrayOutputStream(4096);

    public WrappedHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        this.flag = true;
        return new ServletOutputStream() {
            public boolean isReady() {
                return true;
            }

            public void setWriteListener(WriteListener listener) {
            }

            public void write(int b) throws IOException {
                WrappedHttpServletResponse.this.output.write(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.flag) {
            throw new IOException("Cannot reopen Writer.");
        } else {
            this.flag = true;
            return new PrintWriter(this.output);
        }
    }
}

