package com.eventboard.controller;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
