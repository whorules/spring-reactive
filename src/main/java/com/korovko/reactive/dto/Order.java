package com.korovko.reactive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  private String phoneNumber;
  private String orderNumber;
  private String productCode;

}
