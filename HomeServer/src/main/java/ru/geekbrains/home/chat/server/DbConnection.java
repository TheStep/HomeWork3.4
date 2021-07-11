package ru.geekbrains.home.chat.server;

import java.sql.*;

public class DbConnection {
    private static Connection connection;
    private static PreparedStatement preparedStatementNick;
    private static PreparedStatement preparedStatementChangeNick;

    public static void main(String[] args) {
        try {
            connect();
        } finally {
            disconnect();
        }
    }

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:HomeServer/src/main/resources/Chatdb.db");
            preparedStatementNick = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
            preparedStatementChangeNick = connection.prepareStatement("UPDATE users SET nickname = 'newNick' WHERE nickname = 'oldNick'");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Невозможно подключиться к БД");
        }
    }

    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                preparedStatementNick.close();
                preparedStatementChangeNick.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static String selectedUsername(String login, String password) {
        String nickname = null;
        try {
            preparedStatementNick.setString(1, login);
            preparedStatementNick.setString(2, password);
            ResultSet rs = preparedStatementNick.executeQuery();
            if (rs.next()) {
                nickname = rs.getString(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nickname;
    }
    public static void changeNickName(String oldNick, String newNick) {
        try {
            preparedStatementChangeNick.setString(1, newNick);
            preparedStatementChangeNick.setString(1, oldNick);
            preparedStatementChangeNick.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
