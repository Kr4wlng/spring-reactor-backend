package com.mitocode.controller;

import com.mitocode.model.Dish;
import com.mitocode.service.IDishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/backpressure")
@RequiredArgsConstructor
public class BackPressureController {

    private IDishService service;

    @GetMapping(value = "/json", produces = "application/json")
    public Flux<Dish> json(){
        return Flux.interval(Duration.ofMillis(100))
                .map(t -> new Dish(UUID.randomUUID().toString(), "Soda", 5.90, true));
    }

    @GetMapping(value = "/event", produces = "text/event-stream")
    public Flux<Dish> eventStream(){
        return Flux.interval(Duration.ofMillis(100))
                .map(t -> new Dish(UUID.randomUUID().toString(), "Soda", 5.90, true));
    }

    @GetMapping(value = "/event2", produces = "text/event-stream")
    public Flux<Dish> eventStream2(){
        return service.findAll().repeat(1000);
    }

    @GetMapping(value = "/json2", produces = "applicaiton/json")
    public Flux<Dish> json2(){
        return service.findAll().repeat(1000);
    }

    @GetMapping("/limitRate")
    public Flux<Integer> limitRate(){
        return Flux.range(1, 100)
                .log()
                .limitRate(10, 4)
                //.limitRate(10) // 75%
                .delayElements(Duration.ofMillis(1));
    }

}
