package com.bitfye.common.snow.id;

public class NotInitializedIdGeneratorException extends RuntimeException {
    public NotInitializedIdGeneratorException() {
        super("ID Generator not initialized , please initialize it before use it. SHARD_ID is required");
    }
}
