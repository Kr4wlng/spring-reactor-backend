package com.mitocode.controller;

import com.mitocode.model.Dish;
import com.mitocode.service.IDishService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final IDishService service;

    @GetMapping
    public Mono<ResponseEntity<Flux<Dish>>> findAll(){
        Flux<Dish> fx = service.findAll();

        return Mono.just(ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx)
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Dish>> findById(@PathVariable String id){
        return service.findById(id)
                .map(e -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                )

                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Dish>> save(@RequestBody Dish dish, final ServerHttpRequest req){
        return service.save(dish)
                .map(e -> ResponseEntity
                        .created(URI.create(req.getURI()+"/"+e.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Dish>> update(@PathVariable String id, @RequestBody Dish dish){
        // dish.setId(id);
        // return service.update(id, dish);
        return Mono.just(dish)
                .map(e -> {
                    e.setId(id);
                    return e;
                })
                .flatMap(e -> service.update(id, e))
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

}
