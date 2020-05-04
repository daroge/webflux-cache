package de.daroge.docdemo.infrastructure.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class NoteRequest {

    private String owner;
    private String title;
    private String message;
}
