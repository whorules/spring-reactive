package com.korovko.reactive.controller;

import com.korovko.reactive.dto.Product;
import com.korovko.reactive.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

  private final UserInfoService userInfoService;

  @GetMapping("/relevant")
  public Mono<Product> getTheMostRelevantProduct(@RequestParam String userId,
      @RequestHeader(required = false) String requestId) {
    return userInfoService.getTheMostRelevantProduct(userId).contextWrite(Context.of("CONTEXT_KEY", requestId));
  }

}
