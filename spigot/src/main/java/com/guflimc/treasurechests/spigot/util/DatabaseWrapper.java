package com.guflimc.treasurechests.spigot.util;

import java.util.UUID;

public class DatabaseWrapper<T> {

    private final UUID uuid = UUID.randomUUID(); // randomize instance
    public final T value;

    public DatabaseWrapper(T value) {
        this.value = value;
    }

}
