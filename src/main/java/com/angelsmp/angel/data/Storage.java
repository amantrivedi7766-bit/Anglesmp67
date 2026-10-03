package com.angelsmp.angel.data;

import java.util.UUID;

/**
 * Module 1 - persistence back-end abstraction.
 * Implemented by the local SQLite file store and the external MySQL store.
 */
public interface Storage {

    /** Opens the connection and ensures the schema exists. */
    void init() throws Exception;

    /** @return the stored profile for the uuid, or {@code null} if none exists. */
    PlayerProfile load(UUID uuid) throws Exception;

    /** Inserts a fresh baseline row (race=ANGEL, element=NONE, level=0, kills=0, deaths=0). */
    void insertBaseline(PlayerProfile profile) throws Exception;

    /** Upserts the full profile. */
    void save(PlayerProfile profile) throws Exception;

    /** Closes any open resources. */
    void close();
}
