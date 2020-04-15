package de.daroge.docdemo.infrastructure;

import de.daroge.docdemo.domain.INoteRepository;
import de.daroge.docdemo.domain.Note;
import de.daroge.docdemo.infrastructure.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Slf4j
@RestController
@RequestMapping("/notes")
public class NoteResource {

    @Value("${postUri}")
    private String postUri;

    @Autowired
    private INoteRepository repository;

    @Autowired
    private NoteService noteService;

    @GetMapping(produces = "application/json")
    ResponseEntity<Flux<Note>> getAll(){
        log.info("all-request received");
        return ResponseEntity.ok().body(Flux.from(repository.getAll()));
    }

    @PostMapping(produces = "application/json",consumes = "application/json")
    ResponseEntity<Mono<Note>> post(@RequestBody Note note) throws Exception{
        log.info("new note with title "+ note.getTitle());
        Map<String, String> params = new HashMap<>();
        CompletionStage<Note> noteCompletionStage = noteService
                .createNote(note)
                .thenApply( nte -> {
                    params.put("id",nte.getId().toString());
                    return note;
                });
        URI uri = UriComponentsBuilder.fromUriString(postUri).buildAndExpand(params).toUri();
        return ResponseEntity.created(uri).body(Mono.fromCompletionStage(noteCompletionStage));
    }

    @GetMapping("/{id}")
    Mono<Note> getById(@PathVariable long id){
        log.info(String.format("getting a note with %d as id",id));
        CompletionStage<Note> noteCompletableFuture = noteService.find(id);
        return Mono.fromCompletionStage(noteCompletableFuture);
    }
}
