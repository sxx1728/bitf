package com.bitfye.common.crypto.signature;

public class BadSignatureException extends RuntimeException {
    public BadSignatureException() {
    }

    public BadSignatureException(String message) {
        super(message);
    }
}

