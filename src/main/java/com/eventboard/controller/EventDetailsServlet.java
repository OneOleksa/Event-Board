package com.eventboard.controller;

import com.eventboard.dto.EventDetailsDto;
import com.eventboard.exception.EventNotFoundException;
import com.eventboard.repository.EventRepository;
import com.eventboard.repository.ParticipantRepository;
import com.eventboard.repository.jdbc.JdbcEventRepository;
import com.eventboard.repository.jdbc.JdbcParticipantRepository;
import com.eventboard.service.EventService;
import com.eventboard.service.EventServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;

@WebServlet("/event")
public class EventDetailsServlet extends HttpServlet {
    private EventService eventService;

    @Override
    public void init() {
        EventRepository eventRepository = new JdbcEventRepository();
        ParticipantRepository participantRepository = new JdbcParticipantRepository();
        eventService = new EventServiceImpl(eventRepository, participantRepository);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idValue = request.getParameter("id");
        if (idValue == null || idValue.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID заходу є обов'язковим");
            return;
        }
        try {
            Long eventId = Long.parseLong(idValue);
            EventDetailsDto eventDetailsDto = eventService.getEventDetails(eventId);
            request.setAttribute("eventDetails", eventDetailsDto);
            request.getRequestDispatcher("/WEB-INF/views/event-details.jsp")
                    .forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID заходу має бути числом");
        } catch (EventNotFoundException e){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
