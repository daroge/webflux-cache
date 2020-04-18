package de.daroge.docdemo.domain.util;

import java.util.HashMap;
import java.util.Map;

public class NoteValidation {

    private Map<String,String> content = new HashMap<>();

    public void add(String field, String messageError){
        content.put(field, messageError);
    }

    public boolean empty(){
        return content.isEmpty();
    }
}
