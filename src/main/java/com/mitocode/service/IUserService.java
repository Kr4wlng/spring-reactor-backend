package com.mitocode.service;

import com.mitocode.security.User;
import reactor.core.publisher.Mono;

public interface IUserService extends ICRUD<User, String> {

    Mono<User> saveHash(User user);
    Mono<com.mitocode.security.User> searchByUser(String username);

}
