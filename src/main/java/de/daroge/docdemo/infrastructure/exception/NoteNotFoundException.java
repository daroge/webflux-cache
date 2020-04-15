package de.daroge.docdemo.infrastructure.exception;

public class NoteNotFoundException  extends Exception{

    public NoteNotFoundException(String msg){
        super(msg);
    }
}
