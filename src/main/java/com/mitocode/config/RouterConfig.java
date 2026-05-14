package com.mitocode.config;

import com.mitocode.handler.ClientHandler;
import com.mitocode.handler.DishHandler;
import com.mitocode.handler.InvoiceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> dishRoutes(DishHandler handler){ // , ClientHandler clientHandler){
        return route(GET("/v2/dishes"), handler::findAll) // req -> handler.findAll(req)
                .andRoute(GET("/v2/dishes/{id}"), handler::findById)
                .andRoute(POST("/v2/dishes"), handler::save)
                .andRoute(PUT("/v2/dishes/{id}"), handler::update)
                .andRoute(DELETE("/v2/dishes/{id}"), handler::delete);
                // .andRoute(GET("/v2/clients/{id}"), clientHandler::findAll)
                // .andRoute(GET("/v2/clients/{id}"), clientHandler::findById)
    }

    @Bean
    public RouterFunction<ServerResponse> clientRoutes(ClientHandler handler){
        return route(GET("/v2/clients"), handler::findAll)
                .andRoute(GET("/v2/clients/{id}"), handler::findById)
                .andRoute(POST("/v2/clients"), handler::save)
                .andRoute(PUT("/v2/clients/{id}"), handler::update)
                .andRoute(DELETE("/v2/clients/{id}"), handler::delete);
    }

    @Bean
    public RouterFunction<ServerResponse> invoiceRoutes(InvoiceHandler handler){
        return route(GET("/v2/invoices"), handler::findAll)
                .andRoute(GET("/v2/invoices/{id}"), handler::findById)
                .andRoute(POST("/v2/invoices"), handler::save)
                .andRoute(PUT("/v2/invoices/{id}"), handler::update)
                .andRoute(DELETE("/v2/invoices/{id}"), handler::delete);
    }

}
