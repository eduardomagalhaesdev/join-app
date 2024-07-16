package br.com.join.web.rest;

import br.com.join.repository.ProdutoRepository;
import br.com.join.service.ProdutoService;
import br.com.join.service.dto.ProdutoDTO;
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
 * REST controller for managing {@link br.com.join.domain.Produto}.
 */
@RestController
@RequestMapping("/api")
public class ProdutoResource {

    private final Logger log = LoggerFactory.getLogger(ProdutoResource.class);

    private static final String ENTITY_NAME = "produto";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProdutoService produtoService;

    private final ProdutoRepository produtoRepository;

    public ProdutoResource(ProdutoService produtoService, ProdutoRepository produtoRepository) {
        this.produtoService = produtoService;
        this.produtoRepository = produtoRepository;
    }

    /**
     * {@code POST  /produtos} : Create a new produto.
     *
     * @param produtoDTO the produtoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new produtoDTO, or with status {@code 400 (Bad Request)} if the produto has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/produtos")
    public Mono<ResponseEntity<ProdutoDTO>> createProduto(@Valid @RequestBody ProdutoDTO produtoDTO) throws URISyntaxException {
        log.debug("REST request to save Produto : {}", produtoDTO);
        if (produtoDTO.getId() != null) {
            throw new BadRequestAlertException("A new produto cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return produtoService
            .save(produtoDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/produtos/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /produtos/:id} : Updates an existing produto.
     *
     * @param id the id of the produtoDTO to save.
     * @param produtoDTO the produtoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated produtoDTO,
     * or with status {@code 400 (Bad Request)} if the produtoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the produtoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/produtos/{id}")
    public Mono<ResponseEntity<ProdutoDTO>> updateProduto(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ProdutoDTO produtoDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Produto : {}, {}", id, produtoDTO);
        if (produtoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, produtoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return produtoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return produtoService
                    .update(produtoDTO)
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
     * {@code PATCH  /produtos/:id} : Partial updates given fields of an existing produto, field will ignore if it is null
     *
     * @param id the id of the produtoDTO to save.
     * @param produtoDTO the produtoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated produtoDTO,
     * or with status {@code 400 (Bad Request)} if the produtoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the produtoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the produtoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/produtos/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<ProdutoDTO>> partialUpdateProduto(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ProdutoDTO produtoDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Produto partially : {}, {}", id, produtoDTO);
        if (produtoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, produtoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return produtoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<ProdutoDTO> result = produtoService.partialUpdate(produtoDTO);

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
     * {@code GET  /produtos} : get all the produtos.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of produtos in body.
     */
    @GetMapping("/produtos")
    public Mono<ResponseEntity<List<ProdutoDTO>>> getAllProdutos(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get a page of Produtos");
        return produtoService
            .countAll()
            .zipWith(produtoService.findAll(pageable).collectList())
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
     * {@code GET  /produtos/:id} : get the "id" produto.
     *
     * @param id the id of the produtoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the produtoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/produtos/{id}")
    public Mono<ResponseEntity<ProdutoDTO>> getProduto(@PathVariable Long id) {
        log.debug("REST request to get Produto : {}", id);
        Mono<ProdutoDTO> produtoDTO = produtoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(produtoDTO);
    }

    /**
     * {@code DELETE  /produtos/:id} : delete the "id" produto.
     *
     * @param id the id of the produtoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/produtos/{id}")
    public Mono<ResponseEntity<Void>> deleteProduto(@PathVariable Long id) {
        log.debug("REST request to delete Produto : {}", id);
        return produtoService
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
