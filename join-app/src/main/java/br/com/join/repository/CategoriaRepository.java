package br.com.join.repository;

import br.com.join.domain.Categoria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Categoria entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CategoriaRepository extends ReactiveCrudRepository<Categoria, Long>, CategoriaRepositoryInternal {
    Flux<Categoria> findAllBy(Pageable pageable);

    @Override
    <S extends Categoria> Mono<S> save(S entity);

    @Override
    Flux<Categoria> findAll();

    @Override
    Mono<Categoria> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface CategoriaRepositoryInternal {
    <S extends Categoria> Mono<S> save(S entity);

    Flux<Categoria> findAllBy(Pageable pageable);

    Flux<Categoria> findAll();

    Mono<Categoria> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Categoria> findAllBy(Pageable pageable, Criteria criteria);

}
