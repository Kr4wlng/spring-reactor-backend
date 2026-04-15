package com.mitocode.controller;

import com.mitocode.model.Dish;
import com.mitocode.service.IDishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<Dish>> save(@RequestBody Dish dish){
        return service.save(dish)
                .map(e -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e)
                );
    }

    @PutMapping("/{id}")
    public Mono<Dish> update(@PathVariable String id, @RequestBody Dish dish){
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
                        .body(e));
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

}
