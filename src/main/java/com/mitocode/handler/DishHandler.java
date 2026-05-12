package com.mitocode.handler;

import com.mitocode.dto.DishDTO;
import com.mitocode.model.Dish;
import com.mitocode.service.IDishService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DishHandler {

    private final IDishService service;
    @Qualifier("/defaultMapper")
    private final ModelMapper modelMapper;

    public Mono<ServerResponse> findAll(ServerRequest request){
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll().map(this::convertToDTO), DishDTO.class);
    }

    private DishDTO convertToDTO(Dish dish){
        return modelMapper.map(dish, DishDTO.class);
    }

    private Dish converToDocument(DishDTO dishDTO){
        return modelMapper.map(dishDTO, Dish.class);
    }

}
