package com.example.limit.semaphoreblocker.manager;



@FunctionalInterface
public interface SemaphoreCall<T> {

    T execute();
}
