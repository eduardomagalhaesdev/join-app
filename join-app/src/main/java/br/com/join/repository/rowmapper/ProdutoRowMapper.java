package br.com.join.repository.rowmapper;

import br.com.join.domain.Produto;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Produto}, with proper type conversions.
 */
@Service
public class ProdutoRowMapper implements BiFunction<Row, String, Produto> {

    private final ColumnConverter converter;

    public ProdutoRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Produto} stored in the database.
     */
    @Override
    public Produto apply(Row row, String prefix) {
        Produto entity = new Produto();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setNome(converter.fromRow(row, prefix + "_nome", String.class));
        entity.setQuantidade(converter.fromRow(row, prefix + "_quantidade", Integer.class));
        entity.setCategoriaId(converter.fromRow(row, prefix + "_categoria_id", Long.class));
        return entity;
    }
}
