package br.com.join.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link br.com.join.domain.Produto} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProdutoDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    private String nome;

    @NotNull(message = "must not be null")
    private Integer quantidade;

    private CategoriaDTO categoria;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public CategoriaDTO getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaDTO categoria) {
        this.categoria = categoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProdutoDTO)) {
            return false;
        }

        ProdutoDTO produtoDTO = (ProdutoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, produtoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProdutoDTO{" +
            "id=" + getId() +
            ", nome='" + getNome() + "'" +
            ", quantidade=" + getQuantidade() +
            ", categoria=" + getCategoria() +
            "}";
    }
}
