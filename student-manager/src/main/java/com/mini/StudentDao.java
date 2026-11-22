package com.mini;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** All SQL lives here: INSERT, SELECT, DELETE using PreparedStatement. */
public class StudentDao {

    public void add(String name, String course, int marks) throws SQLException {
        String sql = "INSERT INTO students(name, course, marks) VALUES (?, ?, ?)";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, course);
            ps.setInt(3, marks);
            ps.executeUpdate();
        }
    }

    public List<Student> search(String keyword) throws SQLException {
        String sql = "SELECT id, name, course, marks FROM students "
                   + "WHERE name LIKE ? OR course LIKE ? ORDER BY marks DESC";
        List<Student> list = new ArrayList<>();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(rs.getInt("id"), rs.getString("name"),
                                         rs.getString("course"), rs.getInt("marks")));
                }
            }
        }
        return list;
    }

    public void delete(int id) throws SQLException {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public double average() throws SQLException {
        try (Connection c = Database.get(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT AVG(marks) FROM students")) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }
}
