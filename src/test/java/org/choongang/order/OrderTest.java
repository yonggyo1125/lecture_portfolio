package org.choongang.order;

import org.choongang.order.constants.OrderStatus;
import org.choongang.order.entities.OrderInfo;
import org.choongang.order.service.OrderInfoService;
import org.choongang.order.service.OrderStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Transactional
//@TestPropertySource(properties = "spring.profiles.active=dev")
public class OrderTest {

    //@Autowired
    //private OrderSaveService orderSaveService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Test
    void test1() {
        Long seq = 1706250801849L;

        orderStatusService.change(seq, OrderStatus.ORDER);

        OrderInfo data = orderInfoService.get(seq);
        OrderStatus status = data.getStatus();
        System.out.println("---- 테스트 ------");
        System.out.println(status);

        data.getOrderItems().forEach(i -> System.out.println(i.getStatus()));
    }

    @Test
    void test2() {
        Long seq = 1706250801849L;
        List<Long> orderItemSeq = Arrays.asList(2L);

        orderStatusService.change(seq, orderItemSeq, OrderStatus.PREPARE);

        OrderInfo data = orderInfoService.get(seq);
        OrderStatus status = data.getStatus();
        System.out.println("---- 테스트 ------");
        System.out.println(status);

        data.getOrderItems().forEach(i -> System.out.println(i.getStatus()));

    }
}
