package com.angelsmp.angel.data;

import java.util.UUID;

/** Phase 1 - persistence back-end abstraction (SQLite file store). */
public interface Storage {

    void init() throws Exception;

    PlayerProfile load(UUID uuid) throws Exception;

    void insertBaseline(PlayerProfile profile) throws Exception;

    void save(PlayerProfile profile) throws Exception;

    void close();
}
