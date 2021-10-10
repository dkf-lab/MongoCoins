package me.dkflab.coins.managers;

import me.dkflab.coins.Coins;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

public class SQLInterface {

    private String host = "127.0.0.1";
    private String port = "3306";
    private String database = "testdb";
    private String username = "root";
    private String password = "root";

    private Coins main;
    public SQLInterface(Coins main) {
        this.main=main;
        host = main.getConfig().getString("host");
        port = main.getConfig().getString("port");
        database = main.getConfig().getString("database");
        username = main.getConfig().getString("username");
        password = main.getConfig().getString("password");
    }

    private Connection connection;

    public boolean isConnected() {
        return (connection != null);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if (!isConnected()) {
            connection = DriverManager.getConnection("jdbc:mysql://" +
                            host + ":" + port + "/" + database + "?useSSL=false",
                    username, password);
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTable() {
        PreparedStatement ps;
        try {
            ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS coins (NAME VARCHAR(100),UUID VARCHAR(100),POINTS INT(100),JOINTIME INT(100),PRIMARY KEY (NAME))");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(Player p) {
        try {
            UUID uuid = p.getUniqueId();
            if (!exists(uuid)) {
                PreparedStatement ps2 = getConnection().prepareStatement("INSERT IGNORE INTO coins (NAME,UUID,JOINTIME) VALUES (?,?,?)");
                ps2.setString(1, p.getName());
                ps2.setString(2,uuid.toString());
                ps2.setInt(3, Math.toIntExact(System.currentTimeMillis() / 1000));
                ps2.executeUpdate();
            }
            if (getFirstJoin(uuid) == 0) {
                PreparedStatement ps3 = getConnection().prepareStatement("UPDATE coins SET JOINTIME=? WHERE UUID=?");
                ps3.setInt(1, Math.toIntExact(System.currentTimeMillis() / 1000));
                ps3.setString(2, uuid.toString());
                ps3.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(UUID uuid) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM coins WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet results = ps.executeQuery();
            if (results.next()) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addPoints(UUID uuid, int points) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("UPDATE coins SET POINTS=? WHERE UUID=?");
            ps.setInt(1, getPoints(uuid) + points);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            main.addPoints(uuid,points);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPoints(UUID uuid) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT POINTS FROM coins WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            int points = 0;
            if (rs.next()) {
                points = rs.getInt("POINTS");
                return points;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getFirstJoin(UUID uuid) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT JOINTIME FROM coins WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            int points = 0;
            if (rs.next()) {
                points = rs.getInt("JOINTIME");
                return points;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void removePlayerJoinTime(String name) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("UPDATE coins SET JOINTIME=NULL WHERE NAME=?");
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
