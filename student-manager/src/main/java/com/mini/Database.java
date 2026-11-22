package com.mini;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** JDBC connection + table creation (SQL DDL). */
public class Database {
    private static final String URL = "jdbc:sqlite:students.db";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() throws SQLException {
        try (Connection c = get(); Statement s = c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    name   TEXT    NOT NULL,
                    course TEXT    NOT NULL,
                    marks  INTEGER NOT NULL
                )""");
        }
    }
}
