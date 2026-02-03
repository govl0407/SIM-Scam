package com.example.be.dto;

public class EventLog {

    private String event;
    private String status; // PENDING | ACCEPTED | REJECTED
    private String answer; // yes | no

    public EventLog(String event) {
        this.event = event;
        this.status = "PENDING";
    }

    public void resolve(String answer) {
        this.answer = answer;
        this.status = answer.equalsIgnoreCase("yes")
                ? "ACCEPTED"
                : "REJECTED";
    }

    public String getEvent() { return event; }
    public String getStatus() { return status; }
    public String getAnswer() { return answer; }
}