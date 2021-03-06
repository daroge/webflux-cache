package de.daroge.docdemo.domain;

import de.daroge.docdemo.infrastructure.exception.NoteNotFoundException;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletionStage;

public interface INoteRepository {
    Publisher<Note> getAll();
    CompletionStage<Note> addNote(Note note);
    CompletionStage<Note> getById(NoteId noteId);
}
