package org.choongang.product.constants;

import org.choongang.commons.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * 상품 상태
 *
 */
public enum ProductStatus {
    SALE(Utils.getMessage("ProductStatus.SALE", "commons")), // 판매중
    OUT_OF_STOCK(Utils.getMessage("ProductStatus.OUT_OF_STOCK", "commons")), // 품절
    PREPARE(Utils.getMessage("ProductStatus.PREPARE", "commons")); // 상품 준비중

    private final String title;

    ProductStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * 상품 상태 목록 : 0 - 상수 문자열, 1 - 한글 문자열
     * @return
     */
    public static List<String[]> getList() {
        return Arrays.asList(
                new String[] {SALE.name(), SALE.title},
                new String[] {OUT_OF_STOCK.name(), OUT_OF_STOCK.title},
                new String[] {PREPARE.name(), PREPARE.title}
        );
    }
}