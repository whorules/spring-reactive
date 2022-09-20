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

import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderInfoController {

  private final OrderInfoService orderInfoService;

  @GetMapping(value = "/relevant", produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<OrderResponse> getTheMostRelevantProduct(@RequestParam final String userId,
      @RequestHeader(required = false) final String requestId) {
    return orderInfoService.getTheMostRelevantProduct(userId)
                           .contextWrite(Context.of("CONTEXT_KEY", extractRequestId(requestId)));
  }

  private String extractRequestId(final String requestId) {
    return Objects.isNull(requestId) ? UUID.randomUUID().toString() : requestId;
  }

}
