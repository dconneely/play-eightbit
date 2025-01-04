package com.davidconneely.eightbit.zx81;

final class ShutdownException extends RuntimeException {
    ShutdownException(final String message) {
        super(message);
    }
}
