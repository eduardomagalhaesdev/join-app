package br.com.join.web.rest;

import br.com.join.repository.CategoriaRepository;
import br.com.join.service.CategoriaService;
import br.com.join.service.dto.CategoriaDTO;
import br.com.join.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link br.com.join.domain.Categoria}.
 */
@RestController
@RequestMapping("/api")
public class CategoriaResource {

    private final Logger log = LoggerFactory.getLogger(CategoriaResource.class);

    private static final String ENTITY_NAME = "categoria";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CategoriaService categoriaService;

    private final CategoriaRepository categoriaRepository;

    public CategoriaResource(CategoriaService categoriaService, CategoriaRepository categoriaRepository) {
        this.categoriaService = categoriaService;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * {@code POST  /categorias} : Create a new categoria.
     *
     * @param categoriaDTO the categoriaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new categoriaDTO, or with status {@code 400 (Bad Request)} if the categoria has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/categorias")
    public Mono<ResponseEntity<CategoriaDTO>> createCategoria(@Valid @RequestBody CategoriaDTO categoriaDTO) throws URISyntaxException {
        log.debug("REST request to save Categoria : {}", categoriaDTO);
        if (categoriaDTO.getId() != null) {
            throw new BadRequestAlertException("A new categoria cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return categoriaService
            .save(categoriaDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/categorias/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /categorias/:id} : Updates an existing categoria.
     *
     * @param id the id of the categoriaDTO to save.
     * @param categoriaDTO the categoriaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated categoriaDTO,
     * or with status {@code 400 (Bad Request)} if the categoriaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the categoriaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/categorias/{id}")
    public Mono<ResponseEntity<CategoriaDTO>> updateCategoria(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CategoriaDTO categoriaDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Categoria : {}, {}", id, categoriaDTO);
        if (categoriaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, categoriaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return categoriaRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return categoriaService
                    .update(categoriaDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /categorias/:id} : Partial updates given fields of an existing categoria, field will ignore if it is null
     *
     * @param id the id of the categoriaDTO to save.
     * @param categoriaDTO the categoriaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated categoriaDTO,
     * or with status {@code 400 (Bad Request)} if the categoriaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the categoriaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the categoriaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/categorias/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<CategoriaDTO>> partialUpdateCategoria(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CategoriaDTO categoriaDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Categoria partially : {}, {}", id, categoriaDTO);
        if (categoriaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, categoriaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return categoriaRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<CategoriaDTO> result = categoriaService.partialUpdate(categoriaDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /categorias} : get all the categorias.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of categorias in body.
     */
    @GetMapping("/categorias")
    public Mono<ResponseEntity<List<CategoriaDTO>>> getAllCategorias(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of Categorias");
        return categoriaService
            .countAll()
            .zipWith(categoriaService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity
                    .ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            UriComponentsBuilder.fromHttpRequest(request),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /categorias/:id} : get the "id" categoria.
     *
     * @param id the id of the categoriaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the categoriaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/categorias/{id}")
    public Mono<ResponseEntity<CategoriaDTO>> getCategoria(@PathVariable Long id) {
        log.debug("REST request to get Categoria : {}", id);
        Mono<CategoriaDTO> categoriaDTO = categoriaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(categoriaDTO);
    }

    /**
     * {@code DELETE  /categorias/:id} : delete the "id" categoria.
     *
     * @param id the id of the categoriaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/categorias/{id}")
    public Mono<ResponseEntity<Void>> deleteCategoria(@PathVariable Long id) {
        log.debug("REST request to delete Categoria : {}", id);
        return categoriaService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
