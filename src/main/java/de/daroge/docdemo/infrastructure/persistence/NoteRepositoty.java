package de.daroge.docdemo.infrastructure.persistence;

import com.github.davidmoten.rx.jdbc.Database;
import com.github.davidmoten.rx.jdbc.QuerySelect;
import de.daroge.docdemo.domain.INoteRepository;
import de.daroge.docdemo.domain.Note;
import de.daroge.docdemo.infrastructure.exception.NoteNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import rx.Observable;
import rx.RxReactiveStreams;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
@Repository
public class NoteRepositoty implements INoteRepository {

    @Value("${postUri}")
    private String uri;

    private Database database;
    NoteRepositoty(Database database){
        this.database = database;
    }


    @Override
    public Publisher<Note> getAll() {
        log.info("getting all notes from the database");
        QuerySelect.Builder builder = database.select("select * from note");
        Observable<Note> observable = getFrom(builder);
        return RxReactiveStreams.toPublisher(observable);
    }

    @Override
    public CompletionStage<Note> addNote(Note note) {
        final long[] key = new long[1];
        CompletableFuture<Note> completableFuture = new CompletableFuture<>();
        Observable<Boolean> begin = database.beginTransaction();
        Observable<Long> observableKey = database.update("INSERT INTO note(title,owner,message) VALUES(?,?,?)")
                .parameters(note.getOwner(),note.getTitle(),note.getMessage()).dependsOn(begin).returnGeneratedKeys()
                .getAs(Long.class);
        observableKey.forEach(in -> key[0] = in);
        QuerySelect.Builder selectBuilder = database.select("SELECT FROM * FROM note WHERE id = ?")
                .parameter(key[0])
                .dependsOn(observableKey);
        log.info("the key "+ key[0]);
        Observable<Note> noteObservable = getFrom(selectBuilder);
        noteObservable.forEach(completableFuture::complete);
        noteObservable.doOnError(completableFuture::completeExceptionally);
        log.info("return the result from database");
        return completableFuture;
    }

    @Override
    public CompletionStage<Note> getById(Long id) {
        CompletableFuture<Note> completableFuture = new CompletableFuture<>();
        QuerySelect.Builder builder = database.select("SELECT * FROM note WHERE id = ?")
                .parameter(id);
        Observable<Note> observable = getFrom(builder);
        observable
                .doOnError(error -> new NoteNotFoundException(String.format("note with id %d not found",id)))
                .forEach(completableFuture::complete);
        return completableFuture;
    }

    private static Observable<Note> getFrom(QuerySelect.Builder builder) {
        return builder.get( rs -> new
                Note(rs.getLong("id"),
                rs.getString("owner"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getString("created")))
                .asObservable();
    }
}
