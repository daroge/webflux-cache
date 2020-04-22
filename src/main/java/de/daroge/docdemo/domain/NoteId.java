package de.daroge.docdemo.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
public class NoteId implements Serializable {
    private Long id;

    public NoteId(Long id) {
        this.id = id;
    }
}
