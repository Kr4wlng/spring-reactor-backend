package com.mitocode.service;

import com.mitocode.model.Dish;
import com.mitocode.repo.IDishRepo;
import com.mitocode.service.impl.DishServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
public class DishServiceTest {

    @MockitoBean
    private IDishService service;

    @MockitoBean
    private IDishRepo repo;

    @BeforeEach
    public void init(){
        service = new DishServiceImpl(repo);
    }

    @Test
    public void findAllTest(){
        Mockito.when(repo.findAll()).thenReturn(Flux.just(new Dish(), new Dish()));
        Flux<Dish> fx = service.findAll();

        assertNotNull(fx);
    }

}
