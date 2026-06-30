<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.eventboard.dto.EventDetailsDto" %>
<%@ page import="com.eventboard.model.Event" %>
<%@ page import="com.eventboard.model.Participant" %>
<%@ page import="java.util.List" %>

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
    EventDetailsDto eventDetails =
            (EventDetailsDto) request.getAttribute("eventDetails");

    if (eventDetails == null) {
        response.sendError(404, "Захід не знайдено");
        return;
    }

    Event event = eventDetails.getEvent();
    List<Participant> participants = eventDetails.getParticipants();

    if (participants == null) {
        participants = List.of();
    }

    String contextPath = request.getContextPath();
    String error = (String) request.getAttribute("error");
    int availableSeats = eventDetails.getAvailableSeats();
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Деталі заходу</title>

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

        .info-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 15px;
            margin-top: 20px;
        }

        .info-box {
            background-color: #f9fafb;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            padding: 15px;
        }

        .info-label {
            color: #6b7280;
            font-size: 13px;
            margin-bottom: 6px;
        }

        .info-value {
            color: #111827;
            font-size: 18px;
            font-weight: bold;
        }

        h2 {
            margin-top: 0;
            color: #111827;
        }

        form {
            display: grid;
            grid-template-columns: 1fr 1fr auto;
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

        button:disabled {
            background-color: #9ca3af;
            cursor: not-allowed;
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
            display: inline-block;
            padding: 6px 12px;
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

        .error {
            background-color: #fee2e2;
            color: #991b1b;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-weight: bold;
        }

        .empty {
            text-align: center;
            color: #6b7280;
            padding: 25px;
        }

        .back-link {
            display: inline-block;
            margin-bottom: 20px;
            color: #2563eb;
            text-decoration: none;
            font-weight: bold;
        }

        .back-link:hover {
            text-decoration: underline;
        }

        .notice {
            background-color: #fef3c7;
            color: #92400e;
            padding: 12px;
            border-radius: 6px;
            font-weight: bold;
        }
    </style>
</head>
<body>

<div class="container">

    <a class="back-link" href="<%= escapeHtml(contextPath) %>/events">
        ← Назад до списку заходів
    </a>

    <div class="header">
        <h1><%= escapeHtml(event.getTitle()) %></h1>
        <p>Детальна інформація про захід та список зареєстрованих учасників</p>
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
        <h2>Інформація про захід</h2>

        <div class="info-grid">
            <div class="info-box">
                <div class="info-label">ID заходу</div>
                <div class="info-value"><%= escapeHtml(event.getId()) %></div>
            </div>

            <div class="info-box">
                <div class="info-label">Дата проведення</div>
                <div class="info-value"><%= escapeHtml(event.getEventDate()) %></div>
            </div>

            <div class="info-box">
                <div class="info-label">Максимум місць</div>
                <div class="info-value"><%= escapeHtml(event.getMaxSeats()) %></div>
            </div>

            <div class="info-box">
                <div class="info-label">Вільно місць</div>
                <div class="info-value">
                    <%
                        if (availableSeats > 0) {
                    %>
                    <span class="badge available"><%= escapeHtml(availableSeats) %> місць</span>
                    <%
                    } else {
                    %>
                    <span class="badge full">Немає місць</span>
                    <%
                        }
                    %>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <h2>Реєстрація студента</h2>

        <%
            if (availableSeats > 0) {
        %>
        <form action="<%= escapeHtml(contextPath) %>/event?id=<%= escapeHtml(event.getId()) %>" method="post">
            <div>
                <label for="studentName">Ім'я студента</label>
                <input type="text"
                       id="studentName"
                       name="studentName"
                       placeholder="Наприклад: Іван Сірко "
                       required>
            </div>

            <div>
                <label for="studentEmail">Email студента</label>
                <input type="email"
                       id="studentEmail"
                       name="studentEmail"
                       placeholder="student@example.com"
                       required>
            </div>

            <div>
                <button type="submit">Зареєструвати</button>
            </div>
        </form>
        <%
        } else {
        %>
        <div class="notice">
            Реєстрація недоступна, тому що вільних місць більше немає.
        </div>
        <%
            }
        %>
    </div>

    <div class="card">
        <h2>Зареєстровані учасники</h2>

        <table>
            <tr>
                <th>ID</th>
                <th>Ім'я студента</th>
                <th>Email</th>
            </tr>

            <%
                if (participants.isEmpty()) {
            %>
            <tr>
                <td colspan="3" class="empty">
                    На цей захід ще ніхто не зареєструвався
                </td>
            </tr>
            <%
            } else {
                for (Participant participant : participants) {
            %>
            <tr>
                <td><%= escapeHtml(participant.getId()) %></td>
                <td><%= escapeHtml(participant.getStudentName()) %></td>
                <td><%= escapeHtml(participant.getStudentEmail()) %></td>
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