package de.daroge.docdemo.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.daroge.docdemo.domain.Note;
import lombok.Getter;

@Getter
public class NoteDto {

    private Long id;
    private String owner;
    private String title;
    private String message;
    private String created;
    @JsonIgnore
    private Note note;

    public NoteDto(Note note){
        this.id = note.getId();
        this.owner = note.getOwner();
        this.title = note.getTitle();
        this.message = note.getMessage();
        this.created = note.getCreated();
        this.note = note;
    }
}
