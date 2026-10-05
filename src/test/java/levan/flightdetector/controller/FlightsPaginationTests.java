package levan.flightdetector.controller;

import levan.flightdetector.repository.AircraftRepository;
import levan.flightdetector.repository.FlightPositionRepository;
import levan.flightdetector.repository.FlightRepository;
import levan.flightdetector.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FlightsPaginationTests {
    private AircraftRepository aircraft;
    private FlightRepository flights;
    private FlightPositionRepository positions;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        aircraft = mock(AircraftRepository.class);
        flights = mock(FlightRepository.class);
        positions = mock(FlightPositionRepository.class);
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setOneIndexedParameters(true);
        resolver.setMaxPageSize(100);
        mvc = MockMvcBuilders.standaloneSetup(new Flights(
                mock(FlightService.class), aircraft, flights, positions))
                .setCustomArgumentResolvers(resolver).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"aircrafts", "flights", "flight-positions", "flights/42/positions", "aircrafts/7/flights"})
    void defaultsToFirstPageWithMetadata(String endpoint) throws Exception {
        stubPages(PageRequest.of(0, 20, Sort.by("id").descending()));
        mvc.perform(get("/api/" + endpoint))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"aircrafts", "flights", "flight-positions", "flights/42/positions", "aircrafts/7/flights"})
    void acceptsExplicitPageAndSize(String endpoint) throws Exception {
        stubPages(PageRequest.of(2, 10, Sort.by("id").descending()));
        mvc.perform(get("/api/" + endpoint).param("page", "3").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number").value(2))
                .andExpect(jsonPath("$.page.size").value(10));
    }

    @ParameterizedTest
    @ValueSource(strings = {"aircrafts", "flights", "flight-positions", "flights/42/positions", "aircrafts/7/flights"})
    void normalizesInvalidPagination(String endpoint) throws Exception {
        stubPages(PageRequest.of(0, 20, Sort.by("id").descending()));
        for (String query : new String[]{"?page=1", "?page=0", "?page=-1", "?size=0", "?page=abc"}) {
            mvc.perform(get("/api/" + endpoint + query))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andExpect(jsonPath("$.page.size").value(20));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"aircrafts", "flights", "flight-positions", "flights/42/positions", "aircrafts/7/flights"})
    void capsPageSizeAndAcceptsSort(String endpoint) throws Exception {
        stubPages(PageRequest.of(0, 100, Sort.by("id").ascending()));
        mvc.perform(get("/api/" + endpoint).param("size", "101").param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(100));
    }

    private void stubPages(PageRequest request) {
        when(aircraft.findAll(request)).thenReturn(Page.empty(request));
        when(flights.findAll(request)).thenReturn(Page.empty(request));
        when(flights.findByAircraft_Id(7L, request)).thenReturn(Page.empty(request));
        when(positions.findAll(request)).thenReturn(Page.empty(request));
        when(positions.findByFlight_Id(42L, request)).thenReturn(Page.empty(request));
    }
}
