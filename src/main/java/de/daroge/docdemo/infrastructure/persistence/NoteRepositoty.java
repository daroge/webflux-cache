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
    public Note addNote(Note note) {
        final int[] key = new int[1];
        final Note[] noteResult = new Note[1];
        Observable<Boolean> begin = database.beginTransaction();
        Observable<Integer> observableKey = database.update("INSERT INTO note(title,owner,message) VALUES(?,?,?)")
                .parameters(note.getOwner(),note.getTitle(),note.getMessage()).dependsOn(begin).returnGeneratedKeys()
                .getAs(Integer.class);
        observableKey.forEach(in -> key[0] = in);
        QuerySelect.Builder selectBuilder = database.select("SELECT FROM * FROM note WHERE id = ?")
                .parameter(key[0])
                .dependsOn(observableKey);
        Observable<Note> noteObservable = getFrom(selectBuilder);
        noteObservable.forEach(nt -> noteResult[0] = nt);
        return noteResult[0];
    }

    @Override
    public CompletionStage<Note> getById(Long id) {
        final Note[] note = new Note[1];
        CompletableFuture<Note> result = new CompletableFuture<>();
        QuerySelect.Builder builder = database.select("SELECT * FROM note WHERE id = ?")
                .parameter(id);
        Observable<Note> observable = getFrom(builder);
        observable
                .doOnError(error -> result.completeExceptionally(new NoteNotFoundException(String.format("note with id %d not found",id))))
                .forEach(nte -> {
            note[0] = nte;
            result.complete(note[0]);
        });
        return result;
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
