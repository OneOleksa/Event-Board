<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.eventboard.dto.EventListItemDto" %>

<%!
    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);
        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            switch (ch) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#x27;");
                    break;
                default:
                    escaped.append(ch);
            }
        }

        return escaped.toString();
    }
%>

<%
    List<EventListItemDto> events =
            (List<EventListItemDto>) request.getAttribute("events");

    if (events == null) {
        events = List.of();
    }

    String contextPath = request.getContextPath();
    String error = (String) request.getAttribute("error");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Event Board</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f8;
            margin: 0;
            padding: 0;
        }

        .container {
            width: 90%;
            max-width: 1100px;
            margin: 40px auto;
        }

        .header {
            background-color: #1f2937;
            color: white;
            padding: 25px;
            border-radius: 10px;
            margin-bottom: 25px;
        }

        .header h1 {
            margin: 0;
            font-size: 32px;
        }

        .header p {
            margin-top: 8px;
            color: #d1d5db;
        }

        .card {
            background-color: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
            margin-bottom: 25px;
        }

        h2 {
            margin-top: 0;
            color: #111827;
        }

        form {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr auto;
            gap: 12px;
            align-items: end;
        }

        label {
            display: block;
            font-size: 14px;
            margin-bottom: 5px;
            color: #374151;
        }

        input {
            width: 100%;
            padding: 10px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            box-sizing: border-box;
        }

        button {
            padding: 11px 18px;
            border: none;
            border-radius: 6px;
            background-color: #2563eb;
            color: white;
            cursor: pointer;
            font-weight: bold;
        }

        button:hover {
            background-color: #1d4ed8;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th {
            background-color: #111827;
            color: white;
            padding: 12px;
            text-align: left;
        }

        td {
            padding: 12px;
            border-bottom: 1px solid #e5e7eb;
        }

        tr:hover {
            background-color: #f9fafb;
        }

        .badge {
            padding: 5px 10px;
            border-radius: 20px;
            font-weight: bold;
            font-size: 13px;
        }

        .available {
            background-color: #dcfce7;
            color: #166534;
        }

        .full {
            background-color: #fee2e2;
            color: #991b1b;
        }

        .link {
            color: #2563eb;
            text-decoration: none;
            font-weight: bold;
        }

        .link:hover {
            text-decoration: underline;
        }

        .empty {
            text-align: center;
            color: #6b7280;
            padding: 25px;
        }

        .error {
            background-color: #fee2e2;
            color: #991b1b;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-weight: bold;
        }
    </style>
</head>
<body>

<div class="container">

    <div class="header">
        <h1>Event Board</h1>
        <p>Список майбутніх університетських заходів</p>
    </div>

    <%
        if (error != null) {
    %>
    <div class="error">
        <%= escapeHtml(error) %>
    </div>
    <%
        }
    %>

    <div class="card">
        <h2>Створити новий захід</h2>

        <form action="<%= escapeHtml(contextPath) %>/events" method="post">
            <div>
                <label for="title">Назва заходу</label>
                <input type="text"
                       id="title"
                       name="title"
                       placeholder="Наприклад: Java Workshop"
                       required>
            </div>

            <div>
                <label for="eventDate">Дата</label>
                <input type="date"
                       id="eventDate"
                       name="eventDate"
                       required>
            </div>

            <div>
                <label for="maxSeats">Максимум місць</label>
                <input type="number"
                       id="maxSeats"
                       name="maxSeats"
                       min="1"
                       required>
            </div>

            <div>
                <button type="submit">Додати</button>
            </div>
        </form>
    </div>

    <div class="card">
        <h2>Майбутні заходи</h2>

        <table>
            <tr>
                <th>ID</th>
                <th>Назва</th>
                <th>Дата</th>
                <th>Максимум місць</th>
                <th>Зареєстровано</th>
                <th>Вільно</th>
                <th>Дія</th>
            </tr>

            <%
                if (events.isEmpty()) {
            %>
            <tr>
                <td colspan="7" class="empty">
                    Поки що немає майбутніх заходів
                </td>
            </tr>
            <%
            } else {
                for (EventListItemDto event : events) {
            %>
            <tr>
                <td><%= escapeHtml(event.getId()) %></td>
                <td><%= escapeHtml(event.getTitle()) %></td>
                <td><%= escapeHtml(event.getEventDate()) %></td>
                <td><%= escapeHtml(event.getMaxSeats()) %></td>
                <td><%= escapeHtml(event.getRegisteredCount()) %></td>

                <td>
                    <%
                        if (event.getAvailableSeats() > 0) {
                    %>
                    <span class="badge available">
                                <%= escapeHtml(event.getAvailableSeats()) %> місць
                            </span>
                    <%
                    } else {
                    %>
                    <span class="badge full">Немає місць</span>
                    <%
                        }
                    %>
                </td>

                <td>
                    <a class="link"
                       href="<%= escapeHtml(contextPath) %>/event?id=<%= escapeHtml(event.getId()) %>">
                        Деталі
                    </a>
                </td>
            </tr>
            <%
                    }
                }
            %>
        </table>
    </div>

</div>

</body>
</html>