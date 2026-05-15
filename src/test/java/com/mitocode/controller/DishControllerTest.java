package com.mitocode.controller;

import com.mitocode.dto.DishDTO;
import com.mitocode.model.Dish;
import com.mitocode.service.IDishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.print.attribute.standard.Media;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(controllers = DishController.class)
public class DishControllerTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private IDishService service;

    @MockitoBean
    @Qualifier("defaultMapper")
    private ModelMapper modelMapper;

    @MockitoBean
    private WebProperties.Resources resources;

    private Dish dish1;
    private Dish dish2;
    private DishDTO dish1DTO;
    private DishDTO dish2DTO;
    private List<Dish> dishes;

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);

        dish1 = new Dish("1", "Soda", 29.9, true);
        dish2 = new Dish("2", "Pizza", 49.9, true);

        dish1DTO = new DishDTO("1", "Soda", 29.9, true);
        dish2DTO = new DishDTO("2", "Pizza", 49.9, true);

        dishes = Arrays.asList(dish1, dish2);

        Mockito.when(service.findAll()).thenReturn(Flux.fromIterable(dishes));
        Mockito.when(modelMapper.map(dish1, DishDTO.class)).thenReturn(dish1DTO);
        Mockito.when(modelMapper.map(dish2, DishDTO.class)).thenReturn(dish2DTO);
        Mockito.when(service.save(any())).thenReturn(Mono.just(dish1));
        Mockito.when(service.update(any(), any())).thenReturn(Mono.just(dish1));
    }

    @Test
    public void findAllTest(){

        // Mockito.when(service.findAll()).thenReturn(Flux.fromIterable(dishes));
        // Mockito.when(modelMapper.map(dish1, DishDTO.class)).thenReturn(dish1DTO);
        // Mockito.when(modelMapper.map(dish2, DishDTO.class)).thenReturn(dish2DTO);

        client.get()
                .uri("/dishes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    public void saveTest(){
        // Mockito.when(service.save(any())).thenReturn(Mono.just(dish1));
        // Mockito.when(modelMapper.map(dish1, DishDTO.class)).thenReturn(dish1DTO);

        client.post()
                .uri("/dishes/")
                .body(Mono.just(dish1DTO), DishDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.nameDish").isNotEmpty()
                .jsonPath("$.priceDish").isNumber()
                .jsonPath("$.statusDish").isBoolean();

    }

    @Test
    public void updateTest(){
        // Mockito.when(service.update(any(), any())).thenReturn(Mono.just(dish1));
        // Mockito.when(modelMapper.map(dish1, DishDTO.class)).thenReturn(dish1DTO);

        client.put()
                .uri("/dishes/" + dish1.getId())
                .body(Mono.just(dish1DTO), DishDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.nameDish").isNotEmpty()
                .jsonPath("$.priceDish").isNumber()
                .jsonPath("$.statusDish").isBoolean();

    }

    @Test
    public void deleteTest(){
        Mockito.when(service.delete(any())).thenReturn(Mono.just(true));

        client.delete()
                .uri("/dishes/" + dish1.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void deleteTestFalse(){
        Mockito.when(service.delete(any())).thenReturn(Mono.just(false));

        client.delete()
                .uri("/dishes/" + dish1.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

}
