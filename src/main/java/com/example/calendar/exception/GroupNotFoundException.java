package com.example.calendar.exception;

public class GroupNotFoundException extends RuntimeException {

    public GroupNotFoundException(Long id) {
        super("캘린더 그룹을 찾을 수 없습니다. id=" + id);
    }
}
