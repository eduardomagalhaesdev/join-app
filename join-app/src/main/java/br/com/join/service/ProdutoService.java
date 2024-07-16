package br.com.join.service;

import br.com.join.domain.Produto;
import br.com.join.repository.ProdutoRepository;
import br.com.join.service.dto.ProdutoDTO;
import br.com.join.service.mapper.ProdutoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Produto}.
 */
@Service
@Transactional
public class ProdutoService {

    private final Logger log = LoggerFactory.getLogger(ProdutoService.class);

    private final ProdutoRepository produtoRepository;

    private final ProdutoMapper produtoMapper;

    public ProdutoService(ProdutoRepository produtoRepository, ProdutoMapper produtoMapper) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
    }

    /**
     * Save a produto.
     *
     * @param produtoDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<ProdutoDTO> save(ProdutoDTO produtoDTO) {
        log.debug("Request to save Produto : {}", produtoDTO);
        return produtoRepository.save(produtoMapper.toEntity(produtoDTO)).map(produtoMapper::toDto);
    }

    /**
     * Update a produto.
     *
     * @param produtoDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<ProdutoDTO> update(ProdutoDTO produtoDTO) {
        log.debug("Request to update Produto : {}", produtoDTO);
        return produtoRepository.save(produtoMapper.toEntity(produtoDTO)).map(produtoMapper::toDto);
    }

    /**
     * Partially update a produto.
     *
     * @param produtoDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<ProdutoDTO> partialUpdate(ProdutoDTO produtoDTO) {
        log.debug("Request to partially update Produto : {}", produtoDTO);

        return produtoRepository
            .findById(produtoDTO.getId())
            .map(existingProduto -> {
                produtoMapper.partialUpdate(existingProduto, produtoDTO);

                return existingProduto;
            })
            .flatMap(produtoRepository::save)
            .map(produtoMapper::toDto);
    }

    /**
     * Get all the produtos.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<ProdutoDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Produtos");
        return produtoRepository.findAllBy(pageable).map(produtoMapper::toDto);
    }

    /**
     * Get all the produtos with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Flux<ProdutoDTO> findAllWithEagerRelationships(Pageable pageable) {
        return produtoRepository.findAllWithEagerRelationships(pageable).map(produtoMapper::toDto);
    }

    /**
     * Returns the number of produtos available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return produtoRepository.count();
    }

    /**
     * Get one produto by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<ProdutoDTO> findOne(Long id) {
        log.debug("Request to get Produto : {}", id);
        return produtoRepository.findOneWithEagerRelationships(id).map(produtoMapper::toDto);
    }

    /**
     * Delete the produto by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Produto : {}", id);
        return produtoRepository.deleteById(id);
    }
}
