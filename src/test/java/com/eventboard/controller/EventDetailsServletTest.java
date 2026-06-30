package com.eventboard.controller;

import com.eventboard.dto.EventDetailsDto;
import com.eventboard.dto.RegisterParticipantRequest;
import com.eventboard.exception.EventNotFoundException;
import com.eventboard.exception.ValidationException;
import com.eventboard.model.Event;
import com.eventboard.service.EventService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventDetailsServletTest {

    private EventService eventService;
    private EventDetailsServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        eventService = mock(EventService.class);
        servlet = new EventDetailsServlet();
        setEventService(servlet, eventService);
    }

    @Test
    void doGet_whenIdIsMissing_sendsBadRequest() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("id")).thenReturn(" ");

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "ID заходу є обов'язковим");
        verify(eventService, never()).getEventDetails(any());
    }

    @Test
    void doGet_whenEventExists_forwardsToDetailsView() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        EventDetailsDto eventDetails = new EventDetailsDto(event(), List.of());

        when(request.getParameter("id")).thenReturn("1");
        when(eventService.getEventDetails(1L)).thenReturn(eventDetails);
        when(request.getRequestDispatcher("/WEB-INF/views/event-details.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("eventDetails", eventDetails);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGet_whenEventDoesNotExist_sendsNotFound() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("id")).thenReturn("99");
        when(eventService.getEventDetails(99L)).thenThrow(new EventNotFoundException("Захід не знайдено"));

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Захід не знайдено");
    }

    @Test
    void doPost_whenRegistrationIsValid_registersParticipantAndRedirects() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("id")).thenReturn("1");
        when(request.getParameter("studentName")).thenReturn("Ivan Sirko");
        when(request.getParameter("studentEmail")).thenReturn("student@gmail.com");
        when(request.getContextPath()).thenReturn("/event-board");

        servlet.doPost(request, response);

        ArgumentCaptor<RegisterParticipantRequest> requestCaptor =
                ArgumentCaptor.forClass(RegisterParticipantRequest.class);
        verify(eventService).registerParticipant(requestCaptor.capture());
        assertEquals(1L, requestCaptor.getValue().getEventId());
        assertEquals("Ivan Sirko", requestCaptor.getValue().getStudentName());
        assertEquals("student@gmail.com", requestCaptor.getValue().getStudentEmail());
        verify(response).sendRedirect("/event-board/event?id=1");
    }

    @Test
    void doPost_whenServiceRejectsRegistration_showsErrorAndForwardsBackToDetailsView() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        EventDetailsDto eventDetails = new EventDetailsDto(event(), List.of());

        when(request.getParameter("id")).thenReturn("1");
        when(request.getParameter("studentName")).thenReturn("Ivan Sirko");
        when(request.getParameter("studentEmail")).thenReturn("student@gmail.com");
        when(eventService.getEventDetails(1L)).thenReturn(eventDetails);
        when(request.getRequestDispatcher("/WEB-INF/views/event-details.jsp")).thenReturn(dispatcher);
        doThrow(new ValidationException("На цей захід більше немає вільних місць"))
                .when(eventService)
                .registerParticipant(any(RegisterParticipantRequest.class));

        servlet.doPost(request, response);

        verify(request).setAttribute("error", "На цей захід більше немає вільних місць");
        verify(request).setAttribute("eventDetails", eventDetails);
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(any());
    }

    private Event event() {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Java Workshop");
        event.setEventDate(LocalDate.now().plusDays(1));
        event.setMaxSeats(20);
        return event;
    }

    private void setEventService(EventDetailsServlet servlet, EventService eventService) throws Exception {
        Field field = EventDetailsServlet.class.getDeclaredField("eventService");
        field.setAccessible(true);
        field.set(servlet, eventService);
    }
}
