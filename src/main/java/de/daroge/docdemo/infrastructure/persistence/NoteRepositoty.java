package de.daroge.docdemo.infrastructure.persistence;

import com.github.davidmoten.rx.jdbc.Database;
import de.daroge.docdemo.domain.INoteRepository;
import de.daroge.docdemo.domain.Note;
import de.daroge.docdemo.domain.NoteId;
import de.daroge.docdemo.infrastructure.exception.NoteNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Repository;
import rx.Observable;
import rx.RxReactiveStreams;

import javax.annotation.PreDestroy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
@Repository
public class NoteRepositoty implements INoteRepository {

    private Database database;
    NoteRepositoty(Database database){
        this.database = database;
    }


    @Override
    public Publisher<Note> getAll() {
        log.info("getting all notes from the database");
        return RxReactiveStreams.toPublisher(database.select("select * from note").get(this::getFrom));
    }

    @Override
    public CompletionStage<Note> addNote(Note note) {
        CompletableFuture<Note> completableFuture = new CompletableFuture<>();
        Observable<Integer> observableKey = database.update("INSERT INTO note (owner,title,message) VALUES (?,?,?)")
                .parameter(note.getOwner())
                .parameter(note.getTitle())
                .parameter(note.getMessage())
                .returnGeneratedKeys()
                .getAs(Integer.class);
        Observable<Note> observableNote = database.select("SELECT * FROM note WHERE id = ?").parameters(observableKey).get(this::getFrom);
        observableNote.forEach(completableFuture::complete);
        observableNote.doOnError(completableFuture::completeExceptionally);
        return completableFuture;
    }

    @Override
    public CompletionStage<Note> getById(NoteId id){
        CompletableFuture<Note> completableFuture = new CompletableFuture<>();
        Observable<Note> noteObservable = database.select("SELECT * FROM note WHERE id = ?")
                .parameter(id.getValue()).get(this::getFrom);
        noteObservable.doOnError(error -> {
            throw new NoteNotFoundException(String.format("note with id %d not found",id.getValue()));
        }).forEach(completableFuture::complete);
        return completableFuture;
    }

    private Note getFrom(ResultSet rs) throws SQLException {
        return new Note(rs.getLong("id"), rs.getString("owner"), rs.getString("title"), rs.getString("message"), rs.getString("created"));
    }

    @PreDestroy
    public void closeDatabase(){
        database.close();
    }
}
