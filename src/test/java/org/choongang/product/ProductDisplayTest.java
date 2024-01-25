package org.choongang.product;

import org.choongang.product.service.DisplayData;
import org.choongang.product.service.ProductDisplayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ProductDisplayTest {

    @Autowired
    private ProductDisplayService productDisplayService;

    @Test
    void test1() {
        DisplayData data = productDisplayService.getDisplayData(1706081745716L);
        System.out.println(data);
    }
}
