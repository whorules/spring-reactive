package com.korovko.reactive.respository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.korovko.reactive.config.ClientConfiguration;
import com.korovko.reactive.dto.Order;
import com.korovko.reactive.dto.Product;
import com.korovko.reactive.repository.ExternalServicesRepository;
import com.korovko.reactive.repository.UserInfoRepository;
import com.korovko.reactive.service.OrderInfoService;
import com.korovko.reactive.utils.WebClientHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;

@Import(ExternalServicesRepositoryTest.Config.class)
@ExtendWith(SpringExtension.class)
class ExternalServicesRepositoryTest {

  private static final String PHONE_NUMBER = "9998887766";
  private static final String PRODUCT_CODE = "3852";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private WireMockServer             server;
  @Autowired
  private ExternalServicesRepository externalServicesRepository;

  @Test
  void getOrdersSuccess() throws JsonProcessingException {
    server.stubFor(get(anyUrl())
        .withQueryParam("phoneNumber", equalTo(PHONE_NUMBER))
        .willReturn(aResponse()
            .withHeader("Content-Type", APPLICATION_NDJSON_VALUE)
            .withBody(objectMapper.writeValueAsBytes(orders()))));

    Flux<Order> result = externalServicesRepository.getOrders(PHONE_NUMBER);

    StepVerifier.create(result).expectNextSequence(orders()).verifyComplete();
  }

  @Test
  void getProductsSuccess() throws JsonProcessingException {
    server.stubFor(get(anyUrl())
        .withQueryParam("productCode", equalTo("3852"))
        .willReturn(aResponse()
            .withHeader("Content-Type", APPLICATION_NDJSON_VALUE)
            .withBody(objectMapper.writeValueAsBytes(products()))));

    Flux<Product> result = externalServicesRepository.getProducts(PRODUCT_CODE);

    StepVerifier.create(result)
                .expectNextSequence(products())
                .verifyComplete();
  }

  @Test
  void emptyWhenProductServiceDoesNotRespond() {
    Flux<Product> result = externalServicesRepository.getProducts("incorroct");

    StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
  }

  private List<Order> orders() {
    return List.of(new Order(PHONE_NUMBER, "Order_0", "3852"), new Order(PHONE_NUMBER, "Order_1", "5256"));
  }

  private List<Product> products() {
    return List.of(new Product("111", "3852", "IceCream", 1666.7), new Product("222", "3852", "Milk", 4767));
  }

  @TestConfiguration
  static class Config {

    @Bean
    public WireMockServer webServer() {
      WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());
      wireMockServer.start();
      return wireMockServer;
    }

    @Bean
    public ClientConfiguration clientConfigurationTest(final WireMockServer webServer) {
      return new ClientConfiguration(new ClientConfiguration.OrderService(webServer.baseUrl()),
          new ClientConfiguration.ProductService(webServer.baseUrl()));
    }

    @Bean
    public WebClientHelper webClientHelper() {
      return new WebClientHelper();
    }

    @Bean
    public ExternalServicesRepository externalServicesRepository(
        @Qualifier("clientConfigurationTest") final ClientConfiguration configuration,
        final WebClientHelper webClientHelper) {
      return new ExternalServicesRepository(configuration, webClientHelper);
    }

    @Bean
    public UserInfoRepository userInfoRepository() {
      return mock(UserInfoRepository.class);
    }

    @Bean
    public OrderInfoService userInfoService(final UserInfoRepository userInfoRepository,
        final ExternalServicesRepository externalServicesRepository) {
      return new OrderInfoService(userInfoRepository, externalServicesRepository);
    }

  }

}
