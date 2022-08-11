package com.bitfye.common.crypto.http;

import com.bitfye.common.crypto.signature.BadSignatureException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public interface ErrorHandler {
    void handle(ServletRequest var1, ServletResponse var2, BadSignatureException var3) throws IOException;
}
