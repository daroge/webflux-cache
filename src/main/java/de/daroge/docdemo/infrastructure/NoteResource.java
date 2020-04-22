package de.daroge.docdemo.infrastructure;

import de.daroge.docdemo.domain.INoteRepository;
import de.daroge.docdemo.domain.Note;
import de.daroge.docdemo.domain.NoteId;
import de.daroge.docdemo.infrastructure.service.NoteService;
import de.daroge.docdemo.infrastructure.util.NoteRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/notes")
public class NoteResource {

    @Autowired
    private INoteRepository repository;

    @Autowired
    private NoteService noteService;

    private final MediaType mediaType = MediaType.APPLICATION_JSON;

    @GetMapping(produces = "application/json")
    ResponseEntity<Flux<Note>> getAll(){
        log.info("all-request received");
        return ResponseEntity.ok().body(Flux.from(repository.getAll()));
    }

    @PostMapping(produces = "application/json")
    Mono<ResponseEntity<Note>> create(@RequestBody NoteRequest request) throws Exception{
        log.info("new note with title "+ request.getTitle());
        return noteService
                .createNote(request)
                .map(note -> ResponseEntity.created(URI.create("/notes/"+note.getId()))
                        .contentType(this.mediaType).body(note));

    }

    @GetMapping("/{id}")
    Mono<Note> getById(@PathVariable long id){
        log.info(String.format("getting a note with %d as id",id));
        return noteService.find(new NoteId(id));
    }
}
