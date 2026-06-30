package com.eventboard.controller;

import com.eventboard.config.ApplicationContext;
import com.eventboard.dto.EventDetailsDto;
import com.eventboard.dto.RegisterParticipantRequest;
import com.eventboard.exception.EventNotFoundException;
import com.eventboard.exception.ValidationException;
import com.eventboard.service.EventService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller for one event details page and participant registration.
 */
@WebServlet("/event")
public class EventDetailsServlet extends HttpServlet {
    private EventService eventService;

    /**
     * Initializes manually wired service dependencies.
     */
    @Override
    public void init() {
        eventService = ApplicationContext.getEventService();
    }

    /**
     * Handles GET /event?id=... and forwards event details to the JSP.
     */
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
        } catch (EventNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Handles POST /event?id=... and applies PRG after successful registration.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String idValue = request.getParameter("id");
        if (idValue == null || idValue.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID заходу є обов'язковим");
            return;
        }
        try {
            Long eventId = Long.parseLong(idValue);

            String studentName = request.getParameter("studentName");
            String studentEmail = request.getParameter("studentEmail");

            RegisterParticipantRequest registerRequest =
                    new RegisterParticipantRequest(eventId, studentName, studentEmail);

            eventService.registerParticipant(registerRequest);

            response.sendRedirect(request.getContextPath() + "/event?id=" + eventId);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID заходу має бути числом");
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (EventNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
