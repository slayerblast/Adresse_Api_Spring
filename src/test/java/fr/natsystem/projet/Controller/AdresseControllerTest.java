package fr.natsystem.projet.Controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.natsystem.projet.controller.AdresseController;
import fr.natsystem.projet.model.TarifCommune;
import fr.natsystem.projet.services.AdresseService;
import fr.natsystem.projet.services.ContourCommuneService;
import fr.natsystem.projet.services.TarifCommuneService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdresseControllerTest {

  private static final double LATITUDE = 48.8052;
  private static final double LONGITUDE = 2.4385;

  private static final int FIRST_PAGE = 0;
  private static final int SEARCH_PAGE = 2;
  private static final int SEARCH_PAGE_SIZE = 15;
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int PARTIAL_SEARCH_PAGE_SIZE = 10;

  private static final int PAGE = 3;
  private static final int PAGE_SIZE = 25;
  private static final long EXPECTED_OFFSET = 75L;

  private AdresseService adresseService;
  private TarifCommuneService tarifCommuneService;
  private ContourCommuneService contourCommuneService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    adresseService = mock(AdresseService.class);
    tarifCommuneService = mock(TarifCommuneService.class);
    contourCommuneService = mock(ContourCommuneService.class);

    AdresseController controller =
        new AdresseController(adresseService, tarifCommuneService, contourCommuneService);

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  void shouldReturnTarifCommuneByCodeInsee() throws Exception {
    TarifCommune tarifCommune = mock(TarifCommune.class);

    when(tarifCommuneService.findByCodeInsee("94046")).thenReturn(Optional.of(tarifCommune));

    mockMvc
        .perform(
            get("/api/adresses/communes/{code_insee}/tarif", "94046")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tarifCommuneService).findByCodeInsee("94046");

    verifyNoInteractions(adresseService, contourCommuneService);
  }

  @Test
  void shouldReturnOkWhenTarifCommuneDoesNotExist() throws Exception {

    when(tarifCommuneService.findByCodeInsee("99999")).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get("/api/adresses/communes/{code_insee}/tarif", "99999")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tarifCommuneService).findByCodeInsee("99999");
  }

  @Test
  void shouldReturnAllCommuneContours() throws Exception {
    when(contourCommuneService.findAll()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/adresses/communes/contour").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(contourCommuneService).findAll();

    verifyNoInteractions(adresseService, tarifCommuneService);
  }

  @Test
  void shouldFindNearbyAddresses() throws Exception {
    when(adresseService.trouverAdressesProches(LATITUDE, LONGITUDE)).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/adresses/proches")
                .param("lat", String.valueOf(LATITUDE))
                .param("lon", String.valueOf(LONGITUDE))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(adresseService).trouverAdressesProches(LATITUDE, LONGITUDE);

    verifyNoInteractions(tarifCommuneService, contourCommuneService);
  }

  @Test
  void shouldReturnBadRequestWhenLatitudeIsMissing() throws Exception {

    mockMvc
        .perform(
            get("/api/adresses/proches")
                .param("lon", String.valueOf(LONGITUDE))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(adresseService, never())
        .trouverAdressesProches(
            org.mockito.ArgumentMatchers.anyDouble(), org.mockito.ArgumentMatchers.anyDouble());
  }

  @Test
  void shouldReturnBadRequestWhenLongitudeIsMissing() throws Exception {

    mockMvc
        .perform(
            get("/api/adresses/proches")
                .param("lat", String.valueOf(LATITUDE))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(adresseService, never())
        .trouverAdressesProches(
            org.mockito.ArgumentMatchers.anyDouble(), org.mockito.ArgumentMatchers.anyDouble());
  }

  @Test
  void shouldReturnBadRequestWhenCoordinatesAreInvalid() throws Exception {

    mockMvc
        .perform(
            get("/api/adresses/proches")
                .param("lat", "invalide")
                .param("lon", String.valueOf(LONGITUDE))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(adresseService, never())
        .trouverAdressesProches(
            org.mockito.ArgumentMatchers.anyDouble(), org.mockito.ArgumentMatchers.anyDouble());
  }

  @Test
  void shouldSearchAddressesWithAllCriteriaAndPagination() throws Exception {

    Pageable expectedPageable = PageRequest.of(SEARCH_PAGE, SEARCH_PAGE_SIZE);

    when(adresseService.rechercher("94700", "Victor Hugo", "Maisons-Alfort", expectedPageable))
        .thenReturn(Page.empty(expectedPageable));

    mockMvc
        .perform(
            get("/api/adresses")
                .param("codePostal", "94700")
                .param("rue", "Victor Hugo")
                .param("commune", "Maisons-Alfort")
                .param("page", String.valueOf(SEARCH_PAGE))
                .param("size", String.valueOf(SEARCH_PAGE_SIZE))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0));

    verify(adresseService).rechercher("94700", "Victor Hugo", "Maisons-Alfort", expectedPageable);
  }

  @Test
  void shouldUseDefaultPaginationWhenPageParametersAreAbsent() throws Exception {

    Pageable expectedPageable = PageRequest.of(FIRST_PAGE, DEFAULT_PAGE_SIZE);

    when(adresseService.rechercher(null, null, null, expectedPageable))
        .thenReturn(Page.empty(expectedPageable));

    mockMvc
        .perform(get("/api/adresses").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").value(0));

    verify(adresseService).rechercher(null, null, null, expectedPageable);
  }

  @Test
  void shouldPassPartialSearchCriteriaToService() throws Exception {

    Pageable expectedPageable = PageRequest.of(FIRST_PAGE, PARTIAL_SEARCH_PAGE_SIZE);

    when(adresseService.rechercher(null, "République", null, expectedPageable))
        .thenReturn(Page.empty(expectedPageable));

    mockMvc
        .perform(
            get("/api/adresses")
                .param("rue", "République")
                .param("size", String.valueOf(PARTIAL_SEARCH_PAGE_SIZE))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(adresseService).rechercher(null, "République", null, expectedPageable);
  }

  @Test
  void shouldCreateExpectedPageable() throws Exception {
    Pageable expectedPageable = PageRequest.of(PAGE, PAGE_SIZE);

    when(adresseService.rechercher(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            any(Pageable.class)))
        .thenReturn(Page.empty(expectedPageable));

    mockMvc
        .perform(
            get("/api/adresses")
                .param("page", String.valueOf(PAGE))
                .param("size", String.valueOf(PAGE_SIZE))
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

    verify(adresseService)
        .rechercher(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            pageableCaptor.capture());

    Pageable actualPageable = pageableCaptor.getValue();

    assertThat(actualPageable.getPageNumber()).isEqualTo(PAGE);
    assertThat(actualPageable.getPageSize()).isEqualTo(PAGE_SIZE);
    assertThat(actualPageable.getOffset()).isEqualTo(EXPECTED_OFFSET);
  }

  @Test
  void shouldAutocompleteAddresses() throws Exception {
    String query = "19 rue du Docteur Finlay 75015 Paris";

    when(adresseService.autoComplete(query)).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/adresses/autocomplete").param("q", query).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(adresseService).autoComplete(query);

    verifyNoInteractions(tarifCommuneService, contourCommuneService);
  }

  @Test
  void shouldReturnBadRequestWhenAutocompleteQueryIsMissing() throws Exception {

    mockMvc
        .perform(get("/api/adresses/autocomplete").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    verify(adresseService, never()).autoComplete(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void shouldAllowConfiguredCorsOrigin() throws Exception {
    when(adresseService.autoComplete("Paris")).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/adresses/autocomplete")
                .param("q", "Paris")
                .header("Origin", "http://localhost:4200")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));

    verify(adresseService).autoComplete("Paris");
  }

  @Test
  void shouldRejectUnauthorizedCorsOrigin() throws Exception {
    mockMvc
        .perform(
            get("/api/adresses/autocomplete")
                .param("q", "Paris")
                .header("Origin", "http://localhost:3000")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verify(adresseService, never()).autoComplete(org.mockito.ArgumentMatchers.anyString());
  }
}
