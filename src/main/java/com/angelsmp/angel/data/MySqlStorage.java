package com.angelsmp.angel.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/** External MySQL data-server storage (optional back-end). */
public class MySqlStorage implements Storage {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private Connection connection;

    public MySqlStorage(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public synchronized void init() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true",
                username, password);
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS angel_profiles (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "race VARCHAR(16) NOT NULL, " +
                        "element VARCHAR(16) NOT NULL, " +
                        "level INT NOT NULL, " +
                        "kills INT NOT NULL, " +
                        "deaths INT NOT NULL, " +
                        "active_ts BIGINT NOT NULL, " +
                        "ultimate_ts BIGINT NOT NULL)")) {
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
        save(profile);
    }

    @Override
    public synchronized void save(PlayerProfile profile) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO angel_profiles " +
                        "(uuid, race, element, level, kills, deaths, active_ts, ultimate_ts) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE race = VALUES(race), element = VALUES(element), " +
                        "level = VALUES(level), kills = VALUES(kills), deaths = VALUES(deaths), " +
                        "active_ts = VALUES(active_ts), ultimate_ts = VALUES(ultimate_ts)")) {
            statement.setString(1, profile.getUuid().toString());
            statement.setString(2, profile.getRace().name());
            statement.setString(3, profile.getElement().name());
            statement.setInt(4, profile.getLevel());
            statement.setInt(5, profile.getKills());
            statement.setInt(6, profile.getDeaths());
            statement.setLong(7, profile.getActiveAbilityTimestamp());
            statement.setLong(8, profile.getUltimateAbilityTimestamp());
            statement.executeUpdate();
        }
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
