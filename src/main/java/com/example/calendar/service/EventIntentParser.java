package com.example.calendar.service;

import com.example.calendar.dto.ParsedEventIntent;

public interface EventIntentParser {

    ParsedEventIntent parse(String text);

}
