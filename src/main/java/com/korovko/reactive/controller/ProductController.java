package com.korovko.reactive.controller;

import com.korovko.reactive.dto.OrderResponse;
import com.korovko.reactive.service.OrderInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

  private final OrderInfoService orderInfoService;

  @GetMapping(name = "/relevant", produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<OrderResponse> getTheMostRelevantProduct(@RequestParam String userId,
      @RequestHeader(required = false) String requestId) {
    return orderInfoService.getTheMostRelevantProduct(userId)
                           .contextWrite(Context.of("CONTEXT_KEY", requestId));
  }

}
