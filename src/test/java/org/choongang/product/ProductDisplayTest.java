package org.choongang.product;

import org.choongang.product.service.ProductDisplayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProductDisplayTest {

    @Autowired
    private ProductDisplayService productDisplayService;

    @Test
    void test1() {
        productDisplayService.getDisplayData(1706081745716L);

    }
}
