package com.korovko.reactive.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientHelper {

  public WebClient buildWebClient(final String url) {
    return WebClient.builder()
                    .baseUrl(url)
                    .build();
  }

}
