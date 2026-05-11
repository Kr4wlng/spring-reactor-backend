package com.mitocode.service.impl;

import com.mitocode.pagination.PageSupport;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.ICRUD;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public abstract class CRUDImpl<T, ID> implements ICRUD<T, ID> {

    protected abstract IGenericRepo<T, ID> getRepo();
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<T> save(T t) {
        return getRepo().save(t);
    }

    @Override
    public Mono<T> update(ID id, T t) {
        return getRepo().findById(id).flatMap(e -> getRepo().save(t));
    }

    @Override
    public Flux<T> findAll() {
        return getRepo().findAll();
    }

    @Override
    public Mono<T> findById(ID id) {
        return getRepo().findById(id);
    }

    @Override
    public Mono<Boolean> delete(ID id) {
        return getRepo().deleteById(id)
                .hasElement()
                .flatMap(result -> {
                    if (result){
                        return getRepo().deleteById(id).thenReturn(true);
                    }else{
                        return Mono.just(false);
                    }
                });
    }

    @Override
    public Mono<PageSupport<T>> getPage(Class<T> entityClass, Pageable pageable) {
        // Procesamiento en Backend

        /* int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        // 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        Flux<T> data = getRepo().findAll()
                .skip((long) page + size)
                .take(size); */

        // Procesamiento en MongoDB

        Query query = new Query()
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize());

        Mono<List<T>> pageData = mongoTemplate.find(query, entityClass).collectList();
        Mono<Long> total = mongoTemplate.count(new Query(), entityClass);

        return Mono.zip(pageData, total)
                .map(tuple -> new PageSupport<>(
                        tuple.getT1(),
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        tuple.getT2()
                ));
    }
}
