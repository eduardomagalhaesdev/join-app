package br.com.join.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import br.com.join.IntegrationTest;
import br.com.join.domain.Categoria;
import br.com.join.repository.CategoriaRepository;
import br.com.join.repository.EntityManager;
import br.com.join.service.dto.CategoriaDTO;
import br.com.join.service.mapper.CategoriaMapper;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link CategoriaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class CategoriaResourceIT {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/categorias";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CategoriaMapper categoriaMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Categoria categoria;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Categoria createEntity(EntityManager em) {
        Categoria categoria = new Categoria().nome(DEFAULT_NOME);
        return categoria;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Categoria createUpdatedEntity(EntityManager em) {
        Categoria categoria = new Categoria().nome(UPDATED_NOME);
        return categoria;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Categoria.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
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
        categoria = createEntity(em);
    }

    @Test
    void createCategoria() throws Exception {
        int databaseSizeBeforeCreate = categoriaRepository.findAll().collectList().block().size();
        // Create the Categoria
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(categoria);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeCreate + 1);
        Categoria testCategoria = categoriaList.get(categoriaList.size() - 1);
        assertThat(testCategoria.getNome()).isEqualTo(DEFAULT_NOME);
    }

    @Test
    void createCategoriaWithExistingId() throws Exception {
        // Create the Categoria with an existing ID
        categoria.setId(1L);
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(categoria);

        int databaseSizeBeforeCreate = categoriaRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNomeIsRequired() throws Exception {
        int databaseSizeBeforeTest = categoriaRepository.findAll().collectList().block().size();
        // set the field null
        categoria.setNome(null);

        // Create the Categoria, which fails.
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(categoria);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllCategorias() {
        // Initialize the database
        categoriaRepository.save(categoria).block();

        // Get all the categoriaList
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
            .value(hasItem(categoria.getId().intValue()))
            .jsonPath("$.[*].nome")
            .value(hasItem(DEFAULT_NOME));
    }

    @Test
    void getCategoria() {
        // Initialize the database
        categoriaRepository.save(categoria).block();

        // Get the categoria
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, categoria.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(categoria.getId().intValue()))
            .jsonPath("$.nome")
            .value(is(DEFAULT_NOME));
    }

    @Test
    void getNonExistingCategoria() {
        // Get the categoria
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingCategoria() throws Exception {
        // Initialize the database
        categoriaRepository.save(categoria).block();

        int databaseSizeBeforeUpdate = categoriaRepository.findAll().collectList().block().size();

        // Update the categoria
        Categoria updatedCategoria = categoriaRepository.findById(categoria.getId()).block();
        updatedCategoria.nome(UPDATED_NOME);
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(updatedCategoria);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, categoriaDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeUpdate);
        Categoria testCategoria = categoriaList.get(categoriaList.size() - 1);
        assertThat(testCategoria.getNome()).isEqualTo(UPDATED_NOME);
    }

    @Test
    void putNonExistingCategoria() throws Exception {
        int databaseSizeBeforeUpdate = categoriaRepository.findAll().collectList().block().size();
        categoria.setId(count.incrementAndGet());

        // Create the Categoria
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(categoria);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, categoriaDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchCategoria() throws Exception {
        int databaseSizeBeforeUpdate = categoriaRepository.findAll().collectList().block().size();
        categoria.setId(count.incrementAndGet());

        // Create the Categoria
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(categoria);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamCategoria() throws Exception {
        int databaseSizeBeforeUpdate = categoriaRepository.findAll().collectList().block().size();
        categoria.setId(count.incrementAndGet());

        // Create the Categoria
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(categoria);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCategoriaWithPatch() throws Exception {
        // Initialize the database
        categoriaRepository.save(categoria).block();

        int databaseSizeBeforeUpdate = categoriaRepository.findAll().collectList().block().size();

        // Update the categoria using partial update
        Categoria partialUpdatedCategoria = new Categoria();
        partialUpdatedCategoria.setId(categoria.getId());

        partialUpdatedCategoria.nome(UPDATED_NOME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCategoria.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCategoria))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeUpdate);
        Categoria testCategoria = categoriaList.get(categoriaList.size() - 1);
        assertThat(testCategoria.getNome()).isEqualTo(UPDATED_NOME);
    }

    @Test
    void fullUpdateCategoriaWithPatch() throws Exception {
        // Initialize the database
        categoriaRepository.save(categoria).block();

        int databaseSizeBeforeUpdate = categoriaRepository.findAll().collectList().block().size();

        // Update the categoria using partial update
        Categoria partialUpdatedCategoria = new Categoria();
        partialUpdatedCategoria.setId(categoria.getId());

        partialUpdatedCategoria.nome(UPDATED_NOME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCategoria.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCategoria))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeUpdate);
        Categoria testCategoria = categoriaList.get(categoriaList.size() - 1);
        assertThat(testCategoria.getNome()).isEqualTo(UPDATED_NOME);
    }

    @Test
    void patchNonExistingCategoria() throws Exception {
        int databaseSizeBeforeUpdate = categoriaRepository.findAll().collectList().block().size();
        categoria.setId(count.incrementAndGet());

        // Create the Categoria
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(categoria);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, categoriaDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchCategoria() throws Exception {
        int databaseSizeBeforeUpdate = categoriaRepository.findAll().collectList().block().size();
        categoria.setId(count.incrementAndGet());

        // Create the Categoria
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(categoria);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamCategoria() throws Exception {
        int databaseSizeBeforeUpdate = categoriaRepository.findAll().collectList().block().size();
        categoria.setId(count.incrementAndGet());

        // Create the Categoria
        CategoriaDTO categoriaDTO = categoriaMapper.toDto(categoria);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(categoriaDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Categoria in the database
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteCategoria() {
        // Initialize the database
        categoriaRepository.save(categoria).block();

        int databaseSizeBeforeDelete = categoriaRepository.findAll().collectList().block().size();

        // Delete the categoria
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, categoria.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Categoria> categoriaList = categoriaRepository.findAll().collectList().block();
        assertThat(categoriaList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
