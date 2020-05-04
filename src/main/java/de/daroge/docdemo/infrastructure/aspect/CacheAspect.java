package de.daroge.docdemo.infrastructure.aspect;

import de.daroge.docdemo.domain.Note;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

@Slf4j
@Aspect
@Component
public class CacheAspect {

    @Autowired
    private EmbeddedCacheManager cacheManager;
    private Scheduler scheduler = Schedulers.elastic();
    private Cache<Long, Note> cache;

    public CacheAspect(EmbeddedCacheManager cacheManager){
        cache = cacheManager.getCache("noteCache");
    }

    @Around("execution(* de.daroge.docdemo.infrastructure.service.NoteService.find(..)) && args(id,..)")
    public Mono<Note> findNoteAspect(ProceedingJoinPoint joinPoint, Long id){
        return Mono.fromSupplier(() -> getNote(id)).subscribeOn(scheduler)
                .switchIfEmpty(Mono.defer(() -> {
                    Mono<Note> noteMono = null;
                    try {
                        noteMono = (Mono<Note>) joinPoint.proceed();
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                    return noteMono.flatMap(note -> Mono.fromSupplier(() -> putInCache(note)).subscribeOn(scheduler));
                }));

    }

    @Around("execution(* de.daroge.docdemo.infrastructure.service.NoteService.createNote(..))")
    public Mono<Note> addNoteAspect(ProceedingJoinPoint joinPoint){
        Mono<Note> noteMono = null;
        try {
            noteMono = (Mono<Note>)joinPoint.proceed();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return noteMono.flatMap(nt -> Mono.fromSupplier( () -> putInCache(nt)).subscribeOn(scheduler));
    }

    private Note putInCache(Note note){
        log.info("putting a new item into the cache");
        cache.put(note.getId(),note);
        return note;
    }

    private Note getNote(Long id){
        log.info("looking a note in cache");
        Note note = cache.get(id);
        if(Objects.nonNull(note)){
            log.info("note found in cache");
            return note;
        }
        log.info("note not in cache");
        return null;
    }
}
