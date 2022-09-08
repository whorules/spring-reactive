package com.korovko.reactive.service;

import com.korovko.reactive.config.ClientConfiguration;
import com.korovko.reactive.dto.Order;
import com.korovko.reactive.dto.Product;
import com.korovko.reactive.entity.UserInfo;
import com.korovko.reactive.exception.EntityNotFoundException;
import com.korovko.reactive.repository.UserInfoRepository;
import com.korovko.reactive.utils.WebClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

  private final WebClientHelper     webClientHelper;
  private final UserInfoRepository  repository;
  private final ClientConfiguration clientConfiguration;

  public Mono<Product> getTheMostRelevantProduct(final String userId) {
    return repository
        .findById(userId)
        .onErrorMap(e -> {
          log.error("Unable to find user with id {}", userId);
          return new EntityNotFoundException("Unable to find user with id " + userId, e);
        })
        .map(UserInfo::getPhone)
        .doOnNext(phoneNumber -> log.info("Retrieved phone number {} from user with id {}", phoneNumber, userId))
        .flatMapMany(this::getOrders)
        .map(Order::getProductCode)
        .flatMap(this::getProducts)
        .reduce(this::getHighestScoreProduct);
  }

  private Flux<Order> getOrders(final String phoneNumber) {
    return makeGetRequest(clientConfiguration.orderServiceUrl(), "phoneNumber", phoneNumber)
        .bodyToFlux(Order.class)
        .onErrorMap(e -> {
          log.error("Exception happened after request to order service with user phone number: {}", phoneNumber);
          return new EntityNotFoundException(
              "Exception happened after request to order service with user phone number: " + phoneNumber, e);
        })
        .doOnNext(order -> log.info("Received order for user with phone number {}: {}", phoneNumber, order));
  }

  private Flux<Product> getProducts(final String productCode) {
    return makeGetRequest(clientConfiguration.productServiceUrl(), "productCode", productCode)
        .bodyToFlux(Product.class)
        .onErrorResume(e -> {
          log.error("Exception happened after request to product service with product code: {}", productCode);
          return Flux.empty();
        })
        .doOnNext(product -> log.info("Received product for product code {}: {}", productCode, product));
  }

  private WebClient.ResponseSpec makeGetRequest(final String apiName, final String paramName, final String paramValue) {
    return webClientHelper
        .buildWebClient(apiName)
        .get()
        .uri(builder -> builder.queryParam(paramName, paramValue).build())
        .retrieve();
  }

  private Product getHighestScoreProduct(final Product first, final Product second) {
    return first.getScore() > second.getScore() ? first : second;
  }

}
