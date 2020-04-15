package de.daroge.docdemo.infrastructure.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.daroge.docdemo.domain.INoteRepository;
import de.daroge.docdemo.domain.Note;
import de.daroge.docdemo.domain.util.NoteValidation;
import de.daroge.docdemo.infrastructure.exception.InValidNoteException;
import de.daroge.docdemo.infrastructure.util.Validator;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
@Service
public class NoteService {

    private INoteRepository noteRepository;
    private ObjectMapper objectMapper;
    private DefaultCacheManager cacheManager;
    public NoteService(INoteRepository noteRepository, ObjectMapper mapper, DefaultCacheManager cacheManager){
        this.noteRepository = noteRepository;
        this.objectMapper = mapper;
        this.cacheManager = cacheManager;
    }

    public CompletionStage<Note> createNote(Note note) throws InValidNoteException, JsonProcessingException {
        var validator = new Validator<>(new NoteValidation());
        var entity = note.valid(validator);
        if( !entity.empty() ){
            throw  new InValidNoteException(objectMapper.writeValueAsString(entity));
        }
        return CompletableFuture
                .supplyAsync(() -> putInCache(note))
                .thenApply(nte -> noteRepository.addNote(nte));
    }

    public CompletionStage<Note> find(Long id) {
        return CompletableFuture.supplyAsync(() -> getFromCache(id))
                .thenCompose(note -> {
                    if (note != null) {
                        log.info(String.format("Note with id %d found in cache",note.getId()));
                        return CompletableFuture.completedStage(note);
                    } else {
                        return noteRepository.getById(id);
                    }
                });
    }

    private Note putInCache(Note note){
        log.info("putting a new item into the cache");
        Cache<Long,Note> cache = cacheManager.getCache("default");
        cache.put(note.getId(),note);
        return note;
    }
    private Note getFromCache(Long id){
        log.info("looking item from the cache");
        Cache<Long,Note> cache = cacheManager.getCache("default");
        return cache.get(id);
    }
}