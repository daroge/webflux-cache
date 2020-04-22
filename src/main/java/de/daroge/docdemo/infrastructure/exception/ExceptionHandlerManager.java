package de.daroge.docdemo.infrastructure.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

public class ExceptionHandlerManager implements WebExceptionHandler {

    private static ObjectMapper objectMapper;

    public ExceptionHandlerManager(ObjectMapper mapper){
        objectMapper = mapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        Throwable rootThrowable = getRootException(throwable);
        ExceptionHandler handler = getHandlerFor(rootThrowable);
        if( handler != null){
            handler.handle(rootThrowable,serverWebExchange);
        }
        return Mono.empty();
    }

    private enum ExceptionHandler{

        InvalidNote(){

            @Override
            protected boolean canHandle(Throwable throwable) {
                return throwable instanceof InValidNoteException;
            }

            @Override
            protected void handle(Throwable throwable,ServerWebExchange exchange) {
                var status = HttpStatus.BAD_REQUEST;
                setExchangeResponse(status,exchange,throwable);
            }
        },
        NOteNotFound(){
            @Override
            protected boolean canHandle(Throwable throwable) {
                return throwable instanceof NoteNotFoundException;
            }

            @Override
            protected void handle(Throwable throwable, ServerWebExchange exchange) {
                var status = HttpStatus.NOT_FOUND;
                setExchangeResponse(status,exchange,throwable);
            }
        };

        protected abstract boolean canHandle(Throwable throwable);
        protected  abstract void handle(Throwable throwable,ServerWebExchange exchange);
    }

    private Throwable getRootException(Throwable throwable){
        Throwable ex = throwable;
        while (ex.getCause() != null && ex.getCause() != ex){
            ex = ex.getCause();
        }
        return throwable;
    }

    private ExceptionHandler getHandlerFor(Throwable throwable){
        for (ExceptionHandler exceptionHandler : ExceptionHandler.values()){
            if (exceptionHandler.canHandle(throwable)){
                return  exceptionHandler;
            }
        }
        return null;
    }

    private static Mono<Void> setExchangeResponse( HttpStatus status, ServerWebExchange exchange,Throwable throwable){
        try {
            var error = throwable.getMessage();
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            var db = new DefaultDataBufferFactory().wrap(objectMapper.writeValueAsBytes(error));
            return exchange.getResponse().writeWith(Mono.just(db));
        }catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.empty();
        }
    }
}
