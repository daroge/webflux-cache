package de.daroge.docdemo.domain;

import org.reactivestreams.Publisher;

import java.util.concurrent.CompletionStage;

public interface INoteRepository {
    Publisher<Note> getAll();
    Note addNote(Note note);
    CompletionStage<Note> getById(Long id);
}
