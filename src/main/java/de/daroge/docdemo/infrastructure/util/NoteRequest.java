package de.daroge.docdemo.infrastructure.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NoteRequest {

    private String owner;
    private String title;
    private String message;
}
