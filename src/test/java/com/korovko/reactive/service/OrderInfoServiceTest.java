package com.korovko.reactive.service;

import com.korovko.reactive.dto.Order;
import com.korovko.reactive.dto.OrderResponse;
import com.korovko.reactive.dto.Product;
import com.korovko.reactive.entity.UserInfo;
import com.korovko.reactive.exception.EntityNotFoundException;
import com.korovko.reactive.repository.ExternalServicesRepository;
import com.korovko.reactive.repository.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderInfoServiceTest {

  private static final String USER_ID      = "123";
  private static final String PHONE_NUMBER = "9998887766";

  @Mock
  private UserInfoRepository         repository;
  @Mock
  private ExternalServicesRepository externalServicesRepository;
  @InjectMocks
  private OrderInfoService           service;

  @Test
  void success() {
    when(repository.findById(USER_ID)).thenReturn(Mono.just(new UserInfo(USER_ID, "Alex", PHONE_NUMBER)));
    when(externalServicesRepository.getOrders(PHONE_NUMBER)).thenReturn(Flux.fromIterable(orders()));
    when(externalServicesRepository.getProducts("3852")).thenReturn(Flux.fromIterable(firstProductsList()));
    when(externalServicesRepository.getProducts("5256")).thenReturn(Flux.fromIterable(secondProductsList()));

    Flux<OrderResponse> result = service.getTheMostRelevantProduct(USER_ID);

    StepVerifier.create(result)
        .expectNext(new OrderResponse("Order_0", "Alex", PHONE_NUMBER, "3852", "Milk", "222"))
        .expectNext(new OrderResponse("Order_1", "Alex", PHONE_NUMBER, "5256", "Meal", "333"))
        .expectComplete()
        .verify();
  }

  @Test
  void emptyWhenProductServiceReturnsNull() {
    when(repository.findById(USER_ID)).thenReturn(Mono.just(new UserInfo(USER_ID, "Alex", PHONE_NUMBER)));
    when(externalServicesRepository.getOrders(PHONE_NUMBER)).thenReturn(Flux.fromIterable(orders()));
    when(externalServicesRepository.getProducts("3852")).thenReturn(Flux.empty());
    when(externalServicesRepository.getProducts("5256")).thenReturn(Flux.empty());

    Flux<OrderResponse> result = service.getTheMostRelevantProduct(USER_ID);

    StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
  }

    @Test
    void failsWhenUserIsNotFound() {
      when(repository.findById(USER_ID)).thenReturn(Mono.error(new RuntimeException()));

      Flux<OrderResponse> result = service.getTheMostRelevantProduct(USER_ID);

      StepVerifier.create(result)
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

}
