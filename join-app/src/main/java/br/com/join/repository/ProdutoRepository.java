package br.com.join.repository;

import br.com.join.domain.Produto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Produto entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProdutoRepository extends ReactiveCrudRepository<Produto, Long>, ProdutoRepositoryInternal {
    Flux<Produto> findAllBy(Pageable pageable);

    @Override
    Mono<Produto> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Produto> findAllWithEagerRelationships();

    @Override
    Flux<Produto> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM produto entity WHERE entity.categoria_id = :id")
    Flux<Produto> findByCategoria(Long id);

    @Query("SELECT * FROM produto entity WHERE entity.categoria_id IS NULL")
    Flux<Produto> findAllWhereCategoriaIsNull();

    @Override
    <S extends Produto> Mono<S> save(S entity);

    @Override
    Flux<Produto> findAll();

    @Override
    Mono<Produto> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface ProdutoRepositoryInternal {
    <S extends Produto> Mono<S> save(S entity);

    Flux<Produto> findAllBy(Pageable pageable);

    Flux<Produto> findAll();

    Mono<Produto> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Produto> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Produto> findOneWithEagerRelationships(Long id);

    Flux<Produto> findAllWithEagerRelationships();

    Flux<Produto> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
