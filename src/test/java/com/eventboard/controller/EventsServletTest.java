package com.eventboard.controller;

import com.eventboard.dto.CreateEventRequest;
import com.eventboard.dto.EventListItemDto;
import com.eventboard.exception.ValidationException;
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

class EventsServletTest {

    private EventService eventService;
    private EventsServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        eventService = mock(EventService.class);
        servlet = new EventsServlet();
        setEventService(servlet, eventService);
    }

    @Test
    void doGet_loadsEventsAndForwardsToEventsView() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        List<EventListItemDto> events = List.of(new EventListItemDto(
                1L,
                "Java Workshop",
                LocalDate.now().plusDays(1),
                20,
                3
        ));

        when(eventService.getUpcomingEvents()).thenReturn(events);
        when(request.getRequestDispatcher("/WEB-INF/views/events.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(response).setContentType("text/html;charset=UTF-8");
        verify(request).setAttribute("events", events);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPost_whenEventIsValid_createsEventAndRedirects() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("title")).thenReturn("Java Workshop");
        when(request.getParameter("eventDate")).thenReturn("2026-07-10");
        when(request.getParameter("maxSeats")).thenReturn("20");
        when(request.getContextPath()).thenReturn("/event-board");

        servlet.doPost(request, response);

        ArgumentCaptor<CreateEventRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateEventRequest.class);
        verify(eventService).createEvent(requestCaptor.capture());
        assertEquals("Java Workshop", requestCaptor.getValue().getTitle());
        assertEquals(LocalDate.of(2026, 7, 10), requestCaptor.getValue().getEventDate());
        assertEquals(20, requestCaptor.getValue().getMaxSeats());
        verify(response).sendRedirect("/event-board/events");
    }

    @Test
    void doPost_whenServiceRejectsEvent_showsErrorAndForwardsBackToEventsView() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("title")).thenReturn("Java Workshop");
        when(request.getParameter("eventDate")).thenReturn("2026-07-10");
        when(request.getParameter("maxSeats")).thenReturn("20");
        when(request.getRequestDispatcher("/WEB-INF/views/events.jsp")).thenReturn(dispatcher);
        when(eventService.getUpcomingEvents()).thenReturn(List.of());
        doThrow(new ValidationException("Помилка валідації"))
                .when(eventService)
                .createEvent(any(CreateEventRequest.class));

        servlet.doPost(request, response);

        verify(request).setAttribute("error", "Помилка валідації");
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void doPost_whenMaxSeatsIsNotNumber_showsErrorAndDoesNotCallService() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("title")).thenReturn("Java Workshop");
        when(request.getParameter("eventDate")).thenReturn("2026-07-10");
        when(request.getParameter("maxSeats")).thenReturn("not-number");
        when(request.getRequestDispatcher("/WEB-INF/views/events.jsp")).thenReturn(dispatcher);
        when(eventService.getUpcomingEvents()).thenReturn(List.of());

        servlet.doPost(request, response);

        verify(request).setAttribute("error", "Кількість місць має бути числом");
        verify(eventService, never()).createEvent(any(CreateEventRequest.class));
        verify(dispatcher).forward(request, response);
    }

    private void setEventService(EventsServlet servlet, EventService eventService) throws Exception {
        Field field = EventsServlet.class.getDeclaredField("eventService");
        field.setAccessible(true);
        field.set(servlet, eventService);
    }
}
