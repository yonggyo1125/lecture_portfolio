package org.choongang.product.service;

import lombok.Builder;
import lombok.Data;
import org.choongang.product.entities.Product;

import java.util.List;

@Data
@Builder
public class DisplayData {
    private Long code;
    private String displayName;
    private List<Product> items;
}
