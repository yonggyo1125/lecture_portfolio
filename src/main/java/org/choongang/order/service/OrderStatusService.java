package org.choongang.order.service;

import lombok.RequiredArgsConstructor;
import org.choongang.email.service.EmailSendService;
import org.choongang.order.constants.OrderStatus;
import org.choongang.order.entities.OrderInfo;
import org.choongang.order.entities.OrderItem;
import org.choongang.order.repositories.OrderInfoRepository;
import org.choongang.order.repositories.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderInfoService orderInfoService;
    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;

    private final EmailSendService emailSendService;


    public void change(Long orderSeq, List<Long> orderItemSeq, OrderStatus status) {
        OrderInfo orderInfo = orderInfoService.get(orderSeq);
        List<OrderItem> items = orderInfo.getOrderItems();

        int cnt = 0;
        for (OrderItem item : items) {
            // orderItemSeq가 null이면 전체 주문상품 단계 변경, 값이 있는 경우 일부 주문상품만 변경
            if (orderItemSeq == null || orderItemSeq.contains(item.getSeq())) {
                item.setStatus(status);
            }

            if (orderInfo.getStatus() == item.getStatus()) {
                cnt++;
            }
        }

        orderItemRepository.saveAllAndFlush(items);

        // 주문상품 상태가 모두 동일 -> 주문서 상태도 변경
        if (cnt == items.size()) {
            orderInfo.setStatus(status);
        }

        orderInfoRepository.flush();
    }

    public void change(Long orderSeq, OrderStatus status) {
        change(orderSeq, null, status);
    }
}
