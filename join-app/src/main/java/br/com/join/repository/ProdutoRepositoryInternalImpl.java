package br.com.join.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import br.com.join.domain.Produto;
import br.com.join.repository.rowmapper.CategoriaRowMapper;
import br.com.join.repository.rowmapper.ProdutoRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Produto entity.
 */
@SuppressWarnings("unused")
class ProdutoRepositoryInternalImpl extends SimpleR2dbcRepository<Produto, Long> implements ProdutoRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final CategoriaRowMapper categoriaMapper;
    private final ProdutoRowMapper produtoMapper;

    private static final Table entityTable = Table.aliased("produto", EntityManager.ENTITY_ALIAS);
    private static final Table categoriaTable = Table.aliased("categoria", "categoria");

    public ProdutoRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        CategoriaRowMapper categoriaMapper,
        ProdutoRowMapper produtoMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Produto.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.categoriaMapper = categoriaMapper;
        this.produtoMapper = produtoMapper;
    }

    @Override
    public Flux<Produto> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Produto> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = ProdutoSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(CategoriaSqlHelper.getColumns(categoriaTable, "categoria"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(categoriaTable)
            .on(Column.create("categoria_id", entityTable))
            .equals(Column.create("id", categoriaTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Produto.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Produto> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Produto> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Produto> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Produto> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Produto> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Produto process(Row row, RowMetadata metadata) {
        Produto entity = produtoMapper.apply(row, "e");
        entity.setCategoria(categoriaMapper.apply(row, "categoria"));
        return entity;
    }

    @Override
    public <S extends Produto> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
