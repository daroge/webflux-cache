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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class NoteService {

    private INoteRepository noteRepository;
    private ObjectMapper objectMapper;
    private Scheduler scheduler = Schedulers.elastic();
    private Cache<Long, Note> cache;
    public NoteService(INoteRepository noteRepository, ObjectMapper mapper, DefaultCacheManager cacheManager){
        this.noteRepository = noteRepository;
        this.objectMapper = mapper;
        cache = cacheManager.getCache("default");
    }

    public Mono<Note> createNote(Note note) throws InValidNoteException, JsonProcessingException {
        log.info("validating the node");
        var validator = new Validator<>(new NoteValidation());
        var entity = note.valid(validator);
        if( !entity.empty() ){
            log.info("validation error");
            throw  new InValidNoteException(objectMapper.writeValueAsString(entity));
        }
        return Mono.fromCompletionStage(noteRepository.addNote(note))
                .flatMap(nt -> Mono.fromSupplier( () -> putInCache(nt)).subscribeOn(scheduler));
    }

    public Mono<Note> find(Long id) {
        log.info("in service locking a note");
        return Mono.fromSupplier(() -> getNote(id)).subscribeOn(scheduler)
                .switchIfEmpty(Mono.defer( () -> Mono.fromCompletionStage(noteRepository.getById(id)))
                        .flatMap(note -> Mono.fromSupplier(() -> putInCache(note)).subscribeOn(scheduler)));
    }

    private Note putInCache(Note note){
        log.info("putting a new item into the cache");
        cache.putIfAbsent(note.getId(),note);
        return note;
    }

    private Note getNote(Long id){
        log.info("looking a note in cache");
        Note note = cache.get(id);
        if(note != null){
            log.info("note found in cache");
            return note;
        }
        log.info("note not in cache");
        return null;
    }
}