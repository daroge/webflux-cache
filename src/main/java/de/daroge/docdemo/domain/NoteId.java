package de.daroge.docdemo.domain;

import java.io.Serializable;


public class NoteId implements Serializable {
    private Long id;

    public NoteId(Long id) {
        this.id = id;
    }
    public Long getValue(){
        return id;
    }
}
