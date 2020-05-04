package de.daroge.docdemo.infrastructure;

import de.daroge.docdemo.domain.INoteRepository;
import de.daroge.docdemo.infrastructure.service.NoteDto;
import de.daroge.docdemo.infrastructure.service.NoteService;
import de.daroge.docdemo.infrastructure.util.NoteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/notes")
public class NoteResource {

    @Autowired
    private INoteRepository repository;

    @Autowired
    private NoteService noteService;

    private final MediaType mediaType = MediaType.APPLICATION_JSON;

    @GetMapping(produces = "application/json")
    ResponseEntity<Flux<NoteDto>> getAll(){
        System.out.println("all-request received");
        return ResponseEntity.ok().body(Flux.from(repository.getAll()).map(NoteDto::new));
    }

    @PostMapping(produces = "application/json")
    Mono<ResponseEntity<NoteDto>> create(@RequestBody NoteRequest request, ServerWebExchange exchange) throws Exception{
        System.out.println("new note with title "+ request.getTitle());
        return noteService
                .createNote(request)
                .map(NoteDto::new)
                .map(noteDto -> ResponseEntity.created(URI.create("/notes/"+noteDto.getId())).contentType(this.mediaType).body(noteDto));
    }

    @GetMapping("/{id}")
    Mono<NoteDto> getById(@PathVariable long id){
        System.out.println(String.format("getting a note with %d as id",id));
        return noteService.find(id).map(NoteDto::new);
    }
}
