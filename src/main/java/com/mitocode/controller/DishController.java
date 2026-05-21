package com.mitocode.controller;

import com.mitocode.dto.DishDTO;
import com.mitocode.model.Dish;
import com.mitocode.pagination.PageSupport;
import com.mitocode.service.IDishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final IDishService service;
    @Qualifier("defaultMapper")
    private final ModelMapper modelMapper;

    @PreAuthorize("@authValidator.hasAccess()")
    // @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public Mono<ResponseEntity<Flux<DishDTO>>> findAll(){
        Flux<DishDTO> fx = service.findAll().map(this::convertToDTO);

        return Mono.just(ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx)
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<DishDTO>> findById(@PathVariable String id){
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
    public Mono<ResponseEntity<DishDTO>> save(@Valid @RequestBody DishDTO dto, final ServerHttpRequest req){
        return service.save(converToDocument(dto))
                .map(this::convertToDTO)
                .map(e -> ResponseEntity
                        .created(URI.create(req.getURI() + "/" + e.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<DishDTO>> update(@PathVariable String id, @Valid @RequestBody DishDTO dto){
        // dish.setId(id);
        // return service.update(id, dish);
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

    /* private Dish dishHateoas; */

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<Dish>> getHateoas(@PathVariable String id){
        Mono<Link> monoLink = linkTo(methodOn(DishController.class).findById(id)).withRel("dish-link").toMono();

        // PRÁCTICA NO RECOMENDADA
        /* return service.findById(id)
                .map(e -> EntityModel.of(e, monoLink.block())); */

        // PRACTICA COMUN, PERO NO IDEAL
        /* return service.findById(id)
                .flatMap(e -> {
                    this.dishHateoas = e;
                    return monoLink;
                })
                .map(link -> EntityModel.of(dishHateoas, link)); */

        // PRACTICA INTERMEDIA
        /* return service.findById(id)
                .flatMap(e -> monoLink.map(link -> EntityModel.of(e, link))); */

        // PRACTICA IDEAL
        return service.findById(id)
                .zipWith(monoLink, EntityModel::of);
    }

    @GetMapping("/pageable")
    public Mono<ResponseEntity<PageSupport<DishDTO>>> getPage(
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size
    ){
        return service.getPage(Dish.class, PageRequest.of(page, size))
                .map(ps -> new PageSupport<>(
                        ps.getContent().stream().map(this::convertToDTO).toList(),
                        ps.getPageNumber(),
                        ps.getPageSize(),
                        ps.getTotalElements()
                ))
                .map(e -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private DishDTO convertToDTO(Dish dish){
        return modelMapper.map(dish, DishDTO.class);
    }

    private Dish converToDocument(DishDTO dishDTO){
        return modelMapper.map(dishDTO, Dish.class);
    }

}
