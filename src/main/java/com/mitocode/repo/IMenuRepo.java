package com.mitocode.repo;

import com.mitocode.model.Menu;
import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Flux;

public interface IMenuRepo extends IGenericRepo<Menu, String> {

    @Query("{ 'roles' : { $in: ?0 } }")
    Flux<Menu> getMenus(String[] roles);

}
