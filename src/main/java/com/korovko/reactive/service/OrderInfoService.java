package com.korovko.reactive.service;

import com.korovko.reactive.dto.Order;
import com.korovko.reactive.dto.OrderResponse;
import com.korovko.reactive.dto.Product;
import com.korovko.reactive.entity.UserInfo;
import com.korovko.reactive.exception.EntityNotFoundException;
import com.korovko.reactive.repository.ExternalServicesRepository;
import com.korovko.reactive.repository.UserInfoRepository;
import com.korovko.reactive.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderInfoService {

  private final UserInfoRepository         userInfoRepository;
  private final ExternalServicesRepository externalServicesRepository;

  public Flux<OrderResponse> getTheMostRelevantProduct(final String userId) {
    return userInfoRepository
        .findById(userId)
        .onErrorMap(e -> logAndReturnException(e, userId))
        .doOnNext(userInfo -> LoggingUtils.logOnNext(v -> log.info("Retrieved phone number {} from user with id {}",
            userInfo.getPhone(), userId)))
        .flatMapMany(userInfo -> externalServicesRepository
            .getOrders(userInfo.getPhone())
            .flatMap(order -> externalServicesRepository
                .getProducts(order.getProductCode())
                .reduce(this::getHighestScoreProduct)
                .map(product -> this.buildOrderResponse(userInfo, order, product))));
  }

  private Product getHighestScoreProduct(final Product first, final Product second) {
    return first.getScore() > second.getScore() ? first : second;
  }

  private EntityNotFoundException logAndReturnException(final Throwable throwable, final String userId) {
    LoggingUtils.logOnError(v -> log.error("Unable to find user with id {}", userId));
    return new EntityNotFoundException("Unable to find user with id " + userId, throwable);
  }

  private OrderResponse buildOrderResponse(final UserInfo userInfo, final Order order, final Product product) {
    return new OrderResponse(order.getOrderNumber(), userInfo.getName(), userInfo.getPhone(),
        product.getProductCode(), product.getProductName(), product.getProductId());
  }

}
