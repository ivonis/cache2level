package com.ivo.example.cache.exception;

public class WrongCacheDataException extends CacheException{
    public WrongCacheDataException() {
        super();
    }

    public WrongCacheDataException(String message) {
        super(message);
    }

    public WrongCacheDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongCacheDataException(Throwable cause) {
        super(cause);
    }
}
