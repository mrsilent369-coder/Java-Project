# Student Manager (Java + JDBC + SQL)

Requirements: JDK 17+ and Maven.

Run:
    mvn compile exec:java

Open http://localhost:8080

Structure:
- Database.java   -> JDBC connection + CREATE TABLE
- StudentDao.java -> INSERT / SELECT (LIKE, ORDER BY, AVG) / DELETE with PreparedStatement
- App.java        -> built-in HTTP server that renders the website
- Student.java    -> data model
Data is stored in students.db (SQLite), created automatically.
