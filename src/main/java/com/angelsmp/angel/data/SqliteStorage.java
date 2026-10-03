package com.angelsmp.angel.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/** Local SQLite file storage (the default back-end). */
public class SqliteStorage implements Storage {

    private final File file;
    private Connection connection;

    public SqliteStorage(File file) {
        this.file = file;
    }

    @Override
    public synchronized void init() throws Exception {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IllegalStateException("Could not create data folder for " + file.getName());
        }
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS angel_profiles (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "race TEXT NOT NULL, " +
                        "element TEXT NOT NULL, " +
                        "level INTEGER NOT NULL, " +
                        "kills INTEGER NOT NULL, " +
                        "deaths INTEGER NOT NULL, " +
                        "active_ts INTEGER NOT NULL, " +
                        "ultimate_ts INTEGER NOT NULL)")) {
            statement.executeUpdate();
        }
    }

    @Override
    public synchronized PlayerProfile load(UUID uuid) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT race, element, level, kills, deaths, active_ts, ultimate_ts " +
                        "FROM angel_profiles WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new PlayerProfile(uuid,
                        Race.fromString(rs.getString("race")),
                        Element.fromString(rs.getString("element")),
                        rs.getInt("level"),
                        rs.getInt("kills"),
                        rs.getInt("deaths"),
                        rs.getLong("active_ts"),
                        rs.getLong("ultimate_ts"));
            }
        }
    }

    @Override
    public synchronized void insertBaseline(PlayerProfile profile) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO angel_profiles " +
                        "(uuid, race, element, level, kills, deaths, active_ts, ultimate_ts) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            bind(statement, profile);
            statement.executeUpdate();
        }
    }

    @Override
    public synchronized void save(PlayerProfile profile) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO angel_profiles " +
                        "(uuid, race, element, level, kills, deaths, active_ts, ultimate_ts) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            bind(statement, profile);
            statement.executeUpdate();
        }
    }

    private void bind(PreparedStatement statement, PlayerProfile profile) throws Exception {
        statement.setString(1, profile.getUuid().toString());
        statement.setString(2, profile.getRace().name());
        statement.setString(3, profile.getElement().name());
        statement.setInt(4, profile.getLevel());
        statement.setInt(5, profile.getKills());
        statement.setInt(6, profile.getDeaths());
        statement.setLong(7, profile.getActiveAbilityTimestamp());
        statement.setLong(8, profile.getUltimateAbilityTimestamp());
    }

    @Override
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
                // nothing we can do on shutdown
            }
            connection = null;
        }
    }
}
