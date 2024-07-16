package br.com.join.service;

import br.com.join.domain.Categoria;
import br.com.join.repository.CategoriaRepository;
import br.com.join.service.dto.CategoriaDTO;
import br.com.join.service.mapper.CategoriaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Categoria}.
 */
@Service
@Transactional
public class CategoriaService {

    private final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository categoriaRepository;

    private final CategoriaMapper categoriaMapper;

    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    /**
     * Save a categoria.
     *
     * @param categoriaDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<CategoriaDTO> save(CategoriaDTO categoriaDTO) {
        log.debug("Request to save Categoria : {}", categoriaDTO);
        return categoriaRepository.save(categoriaMapper.toEntity(categoriaDTO)).map(categoriaMapper::toDto);
    }

    /**
     * Update a categoria.
     *
     * @param categoriaDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<CategoriaDTO> update(CategoriaDTO categoriaDTO) {
        log.debug("Request to update Categoria : {}", categoriaDTO);
        return categoriaRepository.save(categoriaMapper.toEntity(categoriaDTO)).map(categoriaMapper::toDto);
    }

    /**
     * Partially update a categoria.
     *
     * @param categoriaDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<CategoriaDTO> partialUpdate(CategoriaDTO categoriaDTO) {
        log.debug("Request to partially update Categoria : {}", categoriaDTO);

        return categoriaRepository
            .findById(categoriaDTO.getId())
            .map(existingCategoria -> {
                categoriaMapper.partialUpdate(existingCategoria, categoriaDTO);

                return existingCategoria;
            })
            .flatMap(categoriaRepository::save)
            .map(categoriaMapper::toDto);
    }

    /**
     * Get all the categorias.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<CategoriaDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Categorias");
        return categoriaRepository.findAllBy(pageable).map(categoriaMapper::toDto);
    }

    /**
     * Returns the number of categorias available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return categoriaRepository.count();
    }

    /**
     * Get one categoria by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<CategoriaDTO> findOne(Long id) {
        log.debug("Request to get Categoria : {}", id);
        return categoriaRepository.findById(id).map(categoriaMapper::toDto);
    }

    /**
     * Delete the categoria by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Categoria : {}", id);
        return categoriaRepository.deleteById(id);
    }
}
