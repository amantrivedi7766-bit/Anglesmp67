package com.angelsmp.angel.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/** Phase 1 - the local SQLite {@code database.db} store. */
public class SqliteStorage implements Storage {

    private final File file;
    private Connection connection;

    public SqliteStorage(File file) {
        this.file = file;
    }

    @Override
    public synchronized void init() throws Exception {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IllegalStateException("Could not create the AngelSMP data folder.");
        }
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS angel_profiles (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "alignment TEXT NOT NULL, " +
                        "tier INTEGER NOT NULL, " +
                        "kills INTEGER NOT NULL, " +
                        "deaths INTEGER NOT NULL, " +
                        "soul_locked_until INTEGER NOT NULL, " +
                        "powers_disabled INTEGER NOT NULL, " +
                        "last_ability_ts INTEGER NOT NULL)")) {
            statement.executeUpdate();
        }
    }

    @Override
    public synchronized PlayerProfile load(UUID uuid) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT alignment, tier, kills, deaths, soul_locked_until, powers_disabled, last_ability_ts " +
                        "FROM angel_profiles WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new PlayerProfile(uuid,
                        Alignment.fromString(rs.getString("alignment")),
                        rs.getInt("tier"),
                        rs.getInt("kills"),
                        rs.getInt("deaths"),
                        rs.getLong("soul_locked_until"),
                        rs.getInt("powers_disabled") != 0,
                        rs.getLong("last_ability_ts"));
            }
        }
    }

    @Override
    public synchronized void insertBaseline(PlayerProfile profile) throws Exception {
        save(profile);
    }

    @Override
    public synchronized void save(PlayerProfile profile) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO angel_profiles " +
                        "(uuid, alignment, tier, kills, deaths, soul_locked_until, powers_disabled, last_ability_ts) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, profile.getUuid().toString());
            statement.setString(2, profile.getAlignment().name());
            statement.setInt(3, profile.getTier());
            statement.setInt(4, profile.getKills());
            statement.setInt(5, profile.getDeaths());
            statement.setLong(6, profile.getSoulLockedUntil());
            statement.setInt(7, profile.isPowersDisabled() ? 1 : 0);
            statement.setLong(8, profile.getLastAbilityTimestamp());
            statement.executeUpdate();
        }
    }

    @Override
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
                // shutdown
            }
            connection = null;
        }
    }
}
