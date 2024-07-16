package br.com.join.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import br.com.join.IntegrationTest;
import br.com.join.domain.Categoria;
import br.com.join.domain.Produto;
import br.com.join.repository.EntityManager;
import br.com.join.repository.ProdutoRepository;
import br.com.join.service.ProdutoService;
import br.com.join.service.dto.ProdutoDTO;
import br.com.join.service.mapper.ProdutoMapper;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link ProdutoResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class ProdutoResourceIT {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final Integer DEFAULT_QUANTIDADE = 1;
    private static final Integer UPDATED_QUANTIDADE = 2;

    private static final String ENTITY_API_URL = "/api/produtos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoRepository produtoRepositoryMock;

    @Autowired
    private ProdutoMapper produtoMapper;

    @Mock
    private ProdutoService produtoServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Produto produto;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Produto createEntity(EntityManager em) {
        Produto produto = new Produto().nome(DEFAULT_NOME).quantidade(DEFAULT_QUANTIDADE);
        // Add required entity
        Categoria categoria;
        categoria = em.insert(CategoriaResourceIT.createEntity(em)).block();
        produto.setCategoria(categoria);
        return produto;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Produto createUpdatedEntity(EntityManager em) {
        Produto produto = new Produto().nome(UPDATED_NOME).quantidade(UPDATED_QUANTIDADE);
        // Add required entity
        Categoria categoria;
        categoria = em.insert(CategoriaResourceIT.createUpdatedEntity(em)).block();
        produto.setCategoria(categoria);
        return produto;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Produto.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        CategoriaResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        produto = createEntity(em);
    }

    @Test
    void createProduto() throws Exception {
        int databaseSizeBeforeCreate = produtoRepository.findAll().collectList().block().size();
        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeCreate + 1);
        Produto testProduto = produtoList.get(produtoList.size() - 1);
        assertThat(testProduto.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testProduto.getQuantidade()).isEqualTo(DEFAULT_QUANTIDADE);
    }

    @Test
    void createProdutoWithExistingId() throws Exception {
        // Create the Produto with an existing ID
        produto.setId(1L);
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        int databaseSizeBeforeCreate = produtoRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNomeIsRequired() throws Exception {
        int databaseSizeBeforeTest = produtoRepository.findAll().collectList().block().size();
        // set the field null
        produto.setNome(null);

        // Create the Produto, which fails.
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkQuantidadeIsRequired() throws Exception {
        int databaseSizeBeforeTest = produtoRepository.findAll().collectList().block().size();
        // set the field null
        produto.setQuantidade(null);

        // Create the Produto, which fails.
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllProdutos() {
        // Initialize the database
        produtoRepository.save(produto).block();

        // Get all the produtoList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(produto.getId().intValue()))
            .jsonPath("$.[*].nome")
            .value(hasItem(DEFAULT_NOME))
            .jsonPath("$.[*].quantidade")
            .value(hasItem(DEFAULT_QUANTIDADE));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProdutosWithEagerRelationshipsIsEnabled() {
        when(produtoServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(produtoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProdutosWithEagerRelationshipsIsNotEnabled() {
        when(produtoServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(produtoRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getProduto() {
        // Initialize the database
        produtoRepository.save(produto).block();

        // Get the produto
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, produto.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(produto.getId().intValue()))
            .jsonPath("$.nome")
            .value(is(DEFAULT_NOME))
            .jsonPath("$.quantidade")
            .value(is(DEFAULT_QUANTIDADE));
    }

    @Test
    void getNonExistingProduto() {
        // Get the produto
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingProduto() throws Exception {
        // Initialize the database
        produtoRepository.save(produto).block();

        int databaseSizeBeforeUpdate = produtoRepository.findAll().collectList().block().size();

        // Update the produto
        Produto updatedProduto = produtoRepository.findById(produto.getId()).block();
        updatedProduto.nome(UPDATED_NOME).quantidade(UPDATED_QUANTIDADE);
        ProdutoDTO produtoDTO = produtoMapper.toDto(updatedProduto);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, produtoDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
        Produto testProduto = produtoList.get(produtoList.size() - 1);
        assertThat(testProduto.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testProduto.getQuantidade()).isEqualTo(UPDATED_QUANTIDADE);
    }

    @Test
    void putNonExistingProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().collectList().block().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, produtoDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().collectList().block().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().collectList().block().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateProdutoWithPatch() throws Exception {
        // Initialize the database
        produtoRepository.save(produto).block();

        int databaseSizeBeforeUpdate = produtoRepository.findAll().collectList().block().size();

        // Update the produto using partial update
        Produto partialUpdatedProduto = new Produto();
        partialUpdatedProduto.setId(produto.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedProduto.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedProduto))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
        Produto testProduto = produtoList.get(produtoList.size() - 1);
        assertThat(testProduto.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testProduto.getQuantidade()).isEqualTo(DEFAULT_QUANTIDADE);
    }

    @Test
    void fullUpdateProdutoWithPatch() throws Exception {
        // Initialize the database
        produtoRepository.save(produto).block();

        int databaseSizeBeforeUpdate = produtoRepository.findAll().collectList().block().size();

        // Update the produto using partial update
        Produto partialUpdatedProduto = new Produto();
        partialUpdatedProduto.setId(produto.getId());

        partialUpdatedProduto.nome(UPDATED_NOME).quantidade(UPDATED_QUANTIDADE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedProduto.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedProduto))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
        Produto testProduto = produtoList.get(produtoList.size() - 1);
        assertThat(testProduto.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testProduto.getQuantidade()).isEqualTo(UPDATED_QUANTIDADE);
    }

    @Test
    void patchNonExistingProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().collectList().block().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, produtoDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().collectList().block().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamProduto() throws Exception {
        int databaseSizeBeforeUpdate = produtoRepository.findAll().collectList().block().size();
        produto.setId(count.incrementAndGet());

        // Create the Produto
        ProdutoDTO produtoDTO = produtoMapper.toDto(produto);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(produtoDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Produto in the database
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteProduto() {
        // Initialize the database
        produtoRepository.save(produto).block();

        int databaseSizeBeforeDelete = produtoRepository.findAll().collectList().block().size();

        // Delete the produto
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, produto.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Produto> produtoList = produtoRepository.findAll().collectList().block();
        assertThat(produtoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
