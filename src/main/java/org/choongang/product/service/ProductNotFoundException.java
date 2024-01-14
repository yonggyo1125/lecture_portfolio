package org.choongang.product.service;

import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertBackException;
import org.springframework.http.HttpStatus;

/**
 * 상품이 조회되지 않는 경우 발생하는 예외
 */
public class ProductNotFoundException extends AlertBackException {
    public ProductNotFoundException() {
        super(Utils.getMessage("NotFound.product", "errors"), HttpStatus.NOT_FOUND);
    }
}
