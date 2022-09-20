package com.korovko.reactive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

  private String orderNumber;
  private String userName;
  private String phoneNumber;
  private String productCode;
  private String productName;
  private String productId;

}
