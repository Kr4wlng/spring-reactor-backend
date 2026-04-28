package com.mitocode.controller;

import com.mitocode.dto.ClientDTO;
import com.mitocode.model.Client;
import com.mitocode.service.IClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final IClientService service;
    private final ModelMapper modelMapper;

    @GetMapping
    public Mono<ResponseEntity<Flux<ClientDTO>>> findAll(){
        Flux<ClientDTO> fx = service.findAll().map(this::convertToDTO);

        return Mono.just(ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx)
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ClientDTO>> findById(@PathVariable String id){
        return service.findById(id)
                .map(this::convertToDTO)
                .map(e -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<ClientDTO>> save(@Valid @RequestBody ClientDTO dto, final ServerHttpRequest req){
        return service.save(converToDocument(dto))
                .map(this::convertToDTO)
                .map(e -> ResponseEntity
                        .created(URI.create(req.getURI() + "/" + e.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ClientDTO>> update(@PathVariable String id, @Valid @RequestBody ClientDTO dto){
        // client.setId(id);
        // return service.update(id, client);
        return Mono.just(dto)
                /* .map(e -> {
                    e.setId(id);
                    return e;
                }) */
                .flatMap(e -> {
                    e.setId(id);
                    return service.update(id,converToDocument(dto));
                })
                .map(this::convertToDTO)
                .map(e -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Boolean>> delete(@PathVariable String id){
        return service.delete(id)
                .flatMap(result -> {
                    if (result){
                        return Mono.just(ResponseEntity.noContent().build());
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }

    /* private Client clientHateoas; */

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<Client>> getHateoas(@PathVariable String id){
        Mono<Link> monoLink = linkTo(methodOn(ClientController.class).findById(id)).withRel("client-link").toMono();

        // PRÁCTICA NO RECOMENDADA
        /* return service.findById(id)
                .map(e -> EntityModel.of(e, monoLink.block())); */

        // PRACTICA COMUN, PERO NO IDEAL
        /* return service.findById(id)
                .flatMap(e -> {
                    this.clientHateoas = e;
                    return monoLink;
                })
                .map(link -> EntityModel.of(clientHateoas, link)); */

        // PRACTICA INTERMEDIA
        /* return service.findById(id)
                .flatMap(e -> monoLink.map(link -> EntityModel.of(e, link))); */

        // PRACTICA IDEAL
        return service.findById(id)
                .zipWith(monoLink, EntityModel::of);
    }

    private ClientDTO convertToDTO(Client client){
        return modelMapper.map(client, ClientDTO.class);
    }

    private Client converToDocument(ClientDTO clientDTO){
        return modelMapper.map(clientDTO, Client.class);
    }

}
