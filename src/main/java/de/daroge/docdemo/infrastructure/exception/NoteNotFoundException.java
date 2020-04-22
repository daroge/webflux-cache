package de.daroge.docdemo.infrastructure.exception;

public class NoteNotFoundException  extends RuntimeException{

    public NoteNotFoundException(String msg){
        super(msg);
    }
}
