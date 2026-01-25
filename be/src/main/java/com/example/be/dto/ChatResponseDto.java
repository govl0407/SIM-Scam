package com.example.be.dto;

import java.util.List;

public class ChatResponseDto {

    private String text;
    private List<EventDto> events;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> events) {
        this.events = events;
    }
}
