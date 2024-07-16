package br.com.join.service.mapper;

import br.com.join.domain.Categoria;
import br.com.join.domain.Produto;
import br.com.join.service.dto.CategoriaDTO;
import br.com.join.service.dto.ProdutoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Produto} and its DTO {@link ProdutoDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProdutoMapper extends EntityMapper<ProdutoDTO, Produto> {
    @Mapping(target = "categoria", source = "categoria", qualifiedByName = "categoriaNome")
    ProdutoDTO toDto(Produto s);

    @Named("categoriaNome")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nome", source = "nome")
    CategoriaDTO toDtoCategoriaNome(Categoria categoria);
}
