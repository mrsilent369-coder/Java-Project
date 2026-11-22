package com.mini;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Mini website: built-in Java HTTP server + JDBC. No frameworks needed. */
public class App {
    static final StudentDao dao = new StudentDao();

    public static void main(String[] args) throws Exception {
        Database.init();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", App::handle);
        server.start();
        System.out.println("Running at http://localhost:8080");
    }

    static void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if (method.equals("POST") && path.equals("/add")) {
                Map<String, String> f = form(ex);
                int marks = Integer.parseInt(f.getOrDefault("marks", "0").trim());
                String name = f.getOrDefault("name", "").trim();
                String course = f.getOrDefault("course", "").trim();
                if (!name.isEmpty() && !course.isEmpty() && marks >= 0 && marks <= 100) {
                    dao.add(name, course, marks);
                }
                redirect(ex, "/");
            } else if (method.equals("POST") && path.equals("/delete")) {
                dao.delete(Integer.parseInt(form(ex).get("id")));
                redirect(ex, "/");
            } else if (method.equals("GET") && path.equals("/")) {
                String q = query(ex.getRequestURI().getRawQuery()).getOrDefault("q", "");
                send(ex, 200, page(q));
            } else {
                send(ex, 404, "<h1>404 Not Found</h1>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, 500, "<h1>Something went wrong</h1><p>" + esc(e.getMessage()) + "</p>");
        }
    }

    static String page(String q) throws Exception {
        List<Student> students = dao.search(q);
        StringBuilder rows = new StringBuilder();
        for (Student s : students) {
            rows.append("<tr><td>").append(esc(s.name())).append("</td><td>")
                .append(esc(s.course())).append("</td><td>").append(s.marks())
                .append("</td><td><form method='post' action='/delete'>")
                .append("<input type='hidden' name='id' value='").append(s.id()).append("'>")
                .append("<button class='del'>Delete</button></form></td></tr>");
        }
        if (students.isEmpty()) rows.append("<tr><td colspan='4'>No students yet.</td></tr>");

        return """
            <!DOCTYPE html><html><head><meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Student Manager</title>
            <style>
              body{font-family:system-ui,sans-serif;max-width:640px;margin:20px auto;padding:0 12px;background:#f5f6fa;color:#222}
              h1{color:#2d5be3} .card{background:#fff;padding:14px;border-radius:10px;margin-bottom:14px;box-shadow:0 1px 4px #0002}
              input{padding:8px;margin:4px 0;width:100%%;box-sizing:border-box;border:1px solid #ccc;border-radius:6px}
              button{padding:8px 14px;border:0;border-radius:6px;background:#2d5be3;color:#fff;cursor:pointer}
              .del{background:#d33;padding:5px 10px}
              table{width:100%%;border-collapse:collapse} td,th{padding:8px;border-bottom:1px solid #eee;text-align:left}
            </style></head><body>
            <h1>🎓 Student Manager</h1>
            <div class="card"><h3>Add student</h3>
              <form method="post" action="/add">
                <input name="name" placeholder="Name" required>
                <input name="course" placeholder="Course" required>
                <input name="marks" type="number" min="0" max="100" placeholder="Marks (0-100)" required>
                <button>Add</button>
              </form></div>
            <div class="card"><form method="get" action="/">
                <input name="q" placeholder="Search name or course" value="%s"> <button>Search</button></form></div>
            <div class="card"><table><tr><th>Name</th><th>Course</th><th>Marks</th><th></th></tr>%s</table>
              <p><b>Class average:</b> %.1f</p></div>
            </body></html>
            """.formatted(esc(q), rows, dao.average());
    }

    // ---------- helpers ----------
    static Map<String, String> form(HttpExchange ex) throws IOException {
        return query(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    static Map<String, String> query(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw == null || raw.isEmpty()) return m;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            m.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                  kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return m;
    }

    static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;")
                                 .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    static void redirect(HttpExchange ex, String to) throws IOException {
        ex.getResponseHeaders().add("Location", to);
        ex.sendResponseHeaders(303, -1);
        ex.close();
    }

    static void send(HttpExchange ex, int code, String html) throws IOException {
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(code, b.length);
        ex.getResponseBody().write(b);
        ex.close();
    }
}
