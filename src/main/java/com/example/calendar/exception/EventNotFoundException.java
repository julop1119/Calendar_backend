package com.example.calendar.exception;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long id) {
        super("일정을 찾을 수 없습니다. id=" + id);
    }
}
