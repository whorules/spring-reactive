//package com.korovko.reactive.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.korovko.reactive.dto.Order;
//import com.korovko.reactive.dto.Product;
//import com.korovko.reactive.entity.UserInfo;
//import com.korovko.reactive.exception.EntityNotFoundException;
//import com.korovko.reactive.repository.UserInfoRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.util.List;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.get;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class OrderInfoServiceTest {
//
//  private static final String USER_ID      = "123";
//  private static final String PHONE_NUMBER = "9998887766";
//
//  private final ObjectMapper objectMapper = new ObjectMapper();
//
//  @Autowired
//  private OrderInfoService   service;
//  @MockBean
//  private UserInfoRepository repository;
//
//  @Test
//  void success() {
//    when(repository.findById(USER_ID)).thenReturn(Mono.just(new UserInfo(USER_ID, "Alex", PHONE_NUMBER)));
//
//    Mono<Product> result = service.getTheMostRelevantProduct(USER_ID);
//
//    StepVerifier.create(result).expectNext(new Product("333", "5256", "Meal", 18889)).expectComplete().verify();
//  }
//
//  @Test
//  void emptyWhenProductServiceDoesNotRespond() {
//    when(repository.findById(USER_ID)).thenReturn(Mono.just(new UserInfo(USER_ID, "Alex", PHONE_NUMBER)));
//
//    Mono<Product> result = service.getTheMostRelevantProduct(USER_ID);
//
//    StepVerifier.create(result).expectNext().expectComplete().verify();
//  }
//
//  @Test
//  void emptyWhenProductServiceReturnEmptyResponse() {
//    when(repository.findById(USER_ID)).thenReturn(Mono.just(new UserInfo(USER_ID, "Alex", PHONE_NUMBER)));
//
//    Mono<Product> result = service.getTheMostRelevantProduct(USER_ID);
//
//    StepVerifier.create(result).expectNext().expectComplete().verify();
//  }
//
//  @Test
//  void failsWhenUserIsNotFound() {
//    when(repository.findById(USER_ID)).thenReturn(Mono.error(new RuntimeException()));
//
//    Mono<Product> result = service.getTheMostRelevantProduct(USER_ID);
//
//    StepVerifier
//        .create(result)
//        .expectErrorMatches(exception -> exception instanceof EntityNotFoundException &&
//            exception.getMessage().equals("Unable to find user with id 123"))
//        .verify();
//  }
//
//  private List<Order> orders() {
//    return List.of(new Order(PHONE_NUMBER, "Order_0", "3852"), new Order(PHONE_NUMBER, "Order_1", "5256"));
//  }
//
//  private List<Product> firstProductsList() {
//    return List.of(new Product("111", "3852", "IceCream", 1666.7), new Product("222", "3852", "Milk", 4767));
//  }
//
//  private List<Product> secondProductsList() {
//    return List.of(new Product("333", "5256", "Meal", 18889), new Product("444", "5256", "Apple", 8766));
//  }
//
//}