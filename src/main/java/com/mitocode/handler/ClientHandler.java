package com.mitocode.handler;

import com.mitocode.dto.ClientDTO;
import com.mitocode.model.Client;
import com.mitocode.service.IClientService;
import com.mitocode.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@RequiredArgsConstructor
public class ClientHandler {

    private final IClientService service;
    @Qualifier("/clientMapper")
    private final ModelMapper modelMapper;
    // private final Validator validator;
    private final RequestValidator requestValidator;

    public Mono<ServerResponse> findAll(ServerRequest request){
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll().map(this::convertToDTO), ClientDTO.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request){
        String id = request.pathVariable("id");

        return service.findById(id)
                .map(this::convertToDTO)
                .flatMap(e -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(e))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> save(ServerRequest request){
        Mono<ClientDTO> monoClientDTO = request.bodyToMono(ClientDTO.class);



        /* return monoClientDTO
                .flatMap(e -> {
                    Errors errors = new BeanPropertyBindingResult(e, ClientDTO.class.getName());
                    validator.validate(e, errors);

                    if (errors.hasErrors()){
                        return Flux.fromIterable(errors.getFieldErrors())
                                .map(error -> new ValidationDTO(error.getField(), error.getDefaultMessage()))
                                .collectList()
                                .flatMap(list -> ServerResponse
                                        .badRequest()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(fromValue(list))
                                );
                    } else {
                        return service.save(this.converToDocument(e))
                                .map(this::convertToDTO)
                                .flatMap(dto -> ServerResponse
                                        .created(URI.create(request.uri().toString().concat("/").concat(e.getId())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(fromValue(dto))
                                );
                    }
                }); */

        return monoClientDTO
                .flatMap(requestValidator::validate)
                .flatMap(e -> service.save(converToDocument(e)))
                .map(this::convertToDTO)
                .flatMap(e -> ServerResponse
                        .created(URI.create(request.uri().toString().concat("/").concat(e.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(e))
                );
    }

    public Mono<ServerResponse> update(ServerRequest request){
        String id = request.pathVariable("id");
        Mono<ClientDTO> monoClientDTO = request.bodyToMono(ClientDTO.class);

        return monoClientDTO
                .map(e -> {
                    e.setId(id);
                    return e;
                })
                .flatMap(e -> service.update(id, converToDocument(e)))
                .map(this::convertToDTO)
                .flatMap(e -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(e))
                );
    }

    public Mono<ServerResponse> delete(ServerRequest request){
        String id = request.pathVariable("id");

        return service.delete(id)
                .flatMap(result -> {
                    if (result){
                        return ServerResponse.noContent().build();
                    } else {
                        return ServerResponse.notFound().build();
                    }
                });
    }

    private ClientDTO convertToDTO(Client client){
        return modelMapper.map(client, ClientDTO.class);
    }

    private Client converToDocument(ClientDTO clientDTO){
        return modelMapper.map(clientDTO, Client.class);
    }

}
