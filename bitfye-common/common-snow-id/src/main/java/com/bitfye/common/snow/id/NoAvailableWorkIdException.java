package com.bitfye.common.snow.id;

public class NoAvailableWorkIdException extends RuntimeException {
    public NoAvailableWorkIdException(String businessContextName) {
        super("No Available WorkId for this businessContextName: " + businessContextName);
    }
}
