package com.motel.exception;

public class IllegalRoomConfigurationException extends RuntimeException {
    public IllegalRoomConfigurationException(String message) {
        super(message);
    }

    public IllegalRoomConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalRoomConfigurationException(Throwable cause) {
        super(cause);
    }

    public IllegalRoomConfigurationException() {
        super();
    }
}
