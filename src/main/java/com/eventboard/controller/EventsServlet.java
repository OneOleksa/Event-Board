package com.eventboard.controller;

import com.eventboard.config.ApplicationContext;
import com.eventboard.dto.CreateEventRequest;
import com.eventboard.dto.EventListItemDto;
import com.eventboard.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import com.eventboard.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/events")
public class EventsServlet extends HttpServlet {
    private EventService eventService;

    @Override
    public void init() {
        eventService = ApplicationContext.getEventService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        List<EventListItemDto> events = eventService.getUpcomingEvents();
        request.setAttribute("events", events);
        request.getRequestDispatcher("/WEB-INF/views/events.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
        String title = request.getParameter("title");
        String eventDateValue = request.getParameter("eventDate");
        String maxSeatsValue = request.getParameter("maxSeats");

        if (eventDateValue == null || eventDateValue.isBlank()) {
                throw new ValidationException("Дата заходу є обов'язковою");
            }

        LocalDate eventDate = LocalDate.parse(eventDateValue);
        int maxSeats = Integer.parseInt(maxSeatsValue);

        CreateEventRequest createEventRequest =
                new CreateEventRequest(title, eventDate, maxSeats);

        eventService.createEvent(createEventRequest);

        response.sendRedirect(request.getContextPath() + "/events");
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Кількість місць має бути числом");
            doGet(request, response);
        } catch (DateTimeParseException e) {
            request.setAttribute("error", "Дата заходу має бути у правильному форматі");
            doGet(request, response);
        }
    }
}