package com.korovko.reactive.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients")
public record ClientConfiguration(OrderService orderService, ProductService productService) {

  public String orderServiceUrl() {
    return orderService.url;
  }

  public String productServiceUrl() {
    return productService.url;
  }

  public record OrderService(String url) {}

  public record ProductService(String url) {}
}


