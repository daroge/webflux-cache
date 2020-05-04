package de.daroge.docdemo.infrastructure.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.daroge.docdemo.domain.INoteRepository;
import de.daroge.docdemo.domain.Note;
import de.daroge.docdemo.domain.NoteId;
import de.daroge.docdemo.domain.util.NoteValidation;
import de.daroge.docdemo.infrastructure.exception.InValidNoteException;
import de.daroge.docdemo.infrastructure.util.NoteRequest;
import de.daroge.docdemo.infrastructure.util.Validator;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@Configurable
public class NoteService {

    private INoteRepository noteRepository;
    private ObjectMapper objectMapper;
    private Scheduler scheduler = Schedulers.elastic();

    public NoteService(INoteRepository noteRepository, ObjectMapper mapper, EmbeddedCacheManager cacheManager){
        this.noteRepository = noteRepository;
        this.objectMapper = mapper;
    }

    public Mono<Note> createNote(NoteRequest noteRequest) throws InValidNoteException, JsonProcessingException {
        Note note = checkRequest(noteRequest);
        return addNote(note);
    }

    public Mono<Note> find(Long id) {
        log.info("in service locking a note");
        return Mono.fromCompletionStage(noteRepository.getById(new NoteId(id)));
    }

    private Note checkRequest(NoteRequest noteRequest) throws InValidNoteException, JsonProcessingException {
        log.info("validating the note");
        var validator = new Validator<>(new NoteValidation());
        Note note = new Note(noteRequest.getOwner(),noteRequest.getTitle(),noteRequest.getMessage());
        var entity = note.valid(validator);
        if( !entity.empty() ){
            log.info("validation error");
            throw  new InValidNoteException(objectMapper.writeValueAsString(entity));
        }
        return note;
    }

    private Mono<Note> addNote(Note note) {
        return Mono.fromCompletionStage(noteRepository.addNote(note));
    }
}