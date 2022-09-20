package com.korovko.reactive.repository;

import com.korovko.reactive.config.ClientConfiguration;
import com.korovko.reactive.dto.Order;
import com.korovko.reactive.dto.Product;
import com.korovko.reactive.exception.EntityNotFoundException;
import com.korovko.reactive.utils.LoggingUtils;
import com.korovko.reactive.utils.WebClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ExternalServicesRepository {

  private final ClientConfiguration clientConfiguration;
  private final WebClientHelper     webClientHelper;

  public Flux<Product> getProducts(final String productCode) {
    return makeGetRequest(clientConfiguration.productServiceUrl(), "productCode", productCode)
        .bodyToFlux(Product.class)
        .onErrorResume(e -> {
          LoggingUtils.logOnError(v -> log.error(
              "Exception happened after request to product service with product code: {}", productCode));
          return Flux.empty();
        })
        .doOnNext(product -> LoggingUtils.logOnNext(v -> log.info("Received product for product code {}: {}",
            productCode, product)))
        .log();
  }

  public Flux<Order> getOrders(final String phoneNumber) {
    return makeGetRequest(clientConfiguration.orderServiceUrl(), "phoneNumber", phoneNumber)
        .bodyToFlux(Order.class)
        .onErrorMap(e -> {
          LoggingUtils.logOnError(v -> log.error("Exception happened after request to order service with user phone number: {}",
              phoneNumber));
          return new EntityNotFoundException(
              "Exception happened after request to order service with user phone number: " + phoneNumber, e);
        })
        .doOnNext(order -> LoggingUtils.logOnNext(v -> log.info("Received order for user with phone number {}: {}",
            phoneNumber, order)))
        .log();
  }

  private WebClient.ResponseSpec makeGetRequest(final String apiName, final String paramName, final String paramValue) {
    return webClientHelper
        .buildWebClient(apiName)
        .get()
        .uri(builder -> builder.queryParam(paramName, paramValue).build())
        .retrieve();
  }

}
