package com.mitocode.repo;

import com.mitocode.security.User;
import reactor.core.publisher.Mono;

public interface IUserRepo extends IGenericRepo<User, String>{

    // SELECT * FROM USER WHERE USERNAME = ?;
    // @Quuery("{ username: ?1 }")
    Mono<User> findOneByUsername(String username);

}
