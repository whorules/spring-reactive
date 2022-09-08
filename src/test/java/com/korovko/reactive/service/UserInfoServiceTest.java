package com.korovko.reactive.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.korovko.reactive.config.ClientConfiguration;
import com.korovko.reactive.dto.Order;
import com.korovko.reactive.dto.Product;
import com.korovko.reactive.entity.UserInfo;
import com.korovko.reactive.exception.EntityNotFoundException;
import com.korovko.reactive.repository.UserInfoRepository;
import com.korovko.reactive.utils.WebClientHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;

@Import(UserInfoServiceTest.Config.class)
@ExtendWith(SpringExtension.class)
class UserInfoServiceTest {

  private static final String USER_ID      = "123";
  private static final String PHONE_NUMBER = "9998887766";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private UserInfoService    service;
  @Autowired
  private WireMockServer     server;
  @MockBean
  private UserInfoRepository repository;

  @Test
  void success() throws JsonProcessingException {
    when(repository.findById(USER_ID)).thenReturn(Mono.just(new UserInfo(USER_ID, "Alex", PHONE_NUMBER)));
    server.stubFor(get(anyUrl())
        .withQueryParam("phoneNumber", equalTo(PHONE_NUMBER))
        .willReturn(aResponse()
            .withHeader("Content-Type", APPLICATION_NDJSON_VALUE)
            .withBody(objectMapper.writeValueAsBytes(orders()))));
    server.stubFor(get(anyUrl())
        .withQueryParam("productCode", equalTo("3852"))
        .willReturn(aResponse()
            .withHeader("Content-Type", APPLICATION_NDJSON_VALUE)
            .withBody(objectMapper.writeValueAsBytes(firstProductsList()))));
    server.stubFor(get(anyUrl())
        .withQueryParam("productCode", equalTo("5256"))
        .willReturn(aResponse()
            .withHeader("Content-Type", APPLICATION_NDJSON_VALUE)
            .withBody(objectMapper.writeValueAsBytes(secondProductsList()))));

    Mono<Product> result = service.getTheMostRelevantProduct(USER_ID);

    StepVerifier.create(result).expectNext(new Product("333", "5256", "Meal", 18889)).expectComplete().verify();
  }

  @Test
  void emptyWhenProductServiceDoesNotRespond() throws JsonProcessingException {
    when(repository.findById(USER_ID)).thenReturn(Mono.just(new UserInfo(USER_ID, "Alex", PHONE_NUMBER)));
    server.stubFor(get(anyUrl())
        .withQueryParam("phoneNumber", equalTo(PHONE_NUMBER))
        .willReturn(aResponse()
            .withHeader("Content-Type", APPLICATION_NDJSON_VALUE)
            .withBody(objectMapper.writeValueAsBytes(orders()))));

    Mono<Product> result = service.getTheMostRelevantProduct(USER_ID);

    StepVerifier.create(result).expectNext().expectComplete().verify();
  }

  @Test
  void emptyWhenProductServiceReturnEmptyResponse() throws JsonProcessingException {
    when(repository.findById(USER_ID)).thenReturn(Mono.just(new UserInfo(USER_ID, "Alex", PHONE_NUMBER)));
    server.stubFor(get(anyUrl())
        .withQueryParam("phoneNumber", equalTo(PHONE_NUMBER))
        .willReturn(aResponse()
            .withHeader("Content-Type", APPLICATION_NDJSON_VALUE)
            .withBody(objectMapper.writeValueAsBytes(orders()))));
    server.stubFor(get(anyUrl())
        .withQueryParam("productCode", equalTo("3852"))
        .willReturn(aResponse().withHeader("Content-Type", APPLICATION_NDJSON_VALUE).withBody(new byte[0])));
    server.stubFor(get(anyUrl())
        .withQueryParam("productCode", equalTo("5256"))
        .willReturn(aResponse().withHeader("Content-Type", APPLICATION_NDJSON_VALUE).withBody(new byte[0])));

    Mono<Product> result = service.getTheMostRelevantProduct(USER_ID);

    StepVerifier.create(result).expectNext().expectComplete().verify();
  }

  @Test
  void failsWhenUserIsNotFound() {
    when(repository.findById(USER_ID)).thenReturn(Mono.error(new RuntimeException()));

    Mono<Product> result = service.getTheMostRelevantProduct(USER_ID);

    StepVerifier
        .create(result)
        .expectErrorMatches(exception -> exception instanceof EntityNotFoundException &&
            exception.getMessage().equals("Unable to find user with id 123"))
        .verify();
  }

  private List<Order> orders() {
    return List.of(new Order(PHONE_NUMBER, "Order_0", "3852"), new Order(PHONE_NUMBER, "Order_1", "5256"));
  }

  private List<Product> firstProductsList() {
    return List.of(new Product("111", "3852", "IceCream", 1666.7), new Product("222", "3852", "Milk", 4767));
  }

  private List<Product> secondProductsList() {
    return List.of(new Product("333", "5256", "Meal", 18889), new Product("444", "5256", "Apple", 8766));
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
    public ClientConfiguration clientConfigurationTest(WireMockServer webServer) {
      return new ClientConfiguration(new ClientConfiguration.OrderService(webServer.baseUrl()),
          new ClientConfiguration.ProductService(webServer.baseUrl()));
    }

    @Bean
    public WebClientHelper webClientHelper() {
      return new WebClientHelper();
    }

    @Bean
    public UserInfoRepository userInfoRepository() {
      return mock(UserInfoRepository.class);
    }

    @Bean
    public UserInfoService userInfoService(WebClientHelper helper, UserInfoRepository repository,
        @Qualifier("clientConfigurationTest") ClientConfiguration configuration) {
      return new UserInfoService(helper, repository, configuration);
    }

  }

}