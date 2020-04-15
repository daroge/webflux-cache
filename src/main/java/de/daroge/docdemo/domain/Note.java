package de.daroge.docdemo.domain;

import de.daroge.docdemo.domain.util.IValidator;
import de.daroge.docdemo.domain.util.NoteValidation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Note {

    private Long id;
    private String owner;
    private String title;
    private String message;
    private String created;

    public Note(Long id,String owner,String title,String message,String created){
        this.id = id;
        this.owner = owner;
        this.title = title;
        this.message = message;
        this.created = created;
    }

    public NoteValidation valid(IValidator<NoteValidation> validator){
        return validator.map(noteValidation -> {
            if( !isTitleValid() ){
                noteValidation.add("title","title is missing");
            }
            return noteValidation;
        }).map(noteValidation -> {
            if (!isMessageValid() ){
                noteValidation.add("message","message is missing");
            }
            return noteValidation;
        }).get();
    }

    private boolean isTitleValid(){
        return title != null;
    }
    private boolean isMessageValid() { return message != null; }
}
