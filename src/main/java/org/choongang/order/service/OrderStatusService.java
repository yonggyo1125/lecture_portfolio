package org.choongang.order.service;

import lombok.RequiredArgsConstructor;
import org.choongang.admin.config.controllers.BasicConfig;
import org.choongang.admin.config.service.ConfigInfoService;
import org.choongang.commons.Utils;
import org.choongang.email.service.EmailMessage;
import org.choongang.email.service.EmailSendService;
import org.choongang.order.constants.OrderStatus;
import org.choongang.order.entities.OrderInfo;
import org.choongang.order.entities.OrderItem;
import org.choongang.order.entities.OrderStatusHistory;
import org.choongang.order.repositories.OrderInfoRepository;
import org.choongang.order.repositories.OrderItemRepository;
import org.choongang.order.repositories.OrderStatusHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderInfoService orderInfoService;
    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    private final EmailSendService emailSendService;
    private final ConfigInfoService configInfoService;

    public void change(Long orderSeq, List<Long> orderItemSeq, OrderStatus status) {
        change(orderSeq, orderItemSeq, status, false);
    }

    public void change(Long orderSeq, List<Long> orderItemSeq, OrderStatus status, boolean manualSendEmail) {

        OrderInfo orderInfo = orderInfoService.get(orderSeq);
        List<OrderItem> items = orderInfo.getOrderItems();

        OrderStatus prevStatus = orderInfo.getStatus(); // 변경 전 상태

        int cnt = 0;
        for (OrderItem item : items) {
            // orderItemSeq가 null이면 전체 주문상품 단계 변경, 값이 있는 경우 일부 주문상품만 변경
            if (orderItemSeq == null || orderItemSeq.contains(item.getSeq())) {
                item.setStatus(status);
            }

            if (orderInfo.getStatus().ordinal() < item.getStatus().ordinal()) {
                cnt++;
            }
        }

        orderItemRepository.saveAllAndFlush(items);

        // 주문상품 상태가 모두 동일 -> 주문서 상태도 변경
        OrderStatus nextStatus = null;
        if (cnt == items.size()) {
            orderInfo.setStatus(status);
            nextStatus = status;
        }

        orderInfoRepository.flush();

        /**
         * 1) 현재 주문상태에서 메일 전송 기록이 없는 경우
         * 2) 관리자 페이지 내에서 수동 전송 하는 경우
         */

        boolean emailSent = orderStatusHistoryRepository.isEmailSent(orderSeq, status);
        if (manualSendEmail) emailSent = false; // 수동 전송일 경우 강제 전송

        if (!emailSent && status.getEmailStatus()) { // 메일 전송 필요 상태
            BasicConfig config = configInfoService.get("config", BasicConfig.class).orElseGet(BasicConfig::new);

            String subject = String.format("[%s][%s] %s",
                        config.getSiteTitle(),
                        Utils.getMessage("OrderStatus." + status.name(), "commons"),
                        Utils.getMessage("안내_메일", "commons")
                    );
            EmailMessage emailMessage = new EmailMessage(orderInfo.getOrderEmail(), subject, subject);

            Map<String, Object> tplData = new HashMap<>();
            tplData.put("orderInfo", orderInfo);
            emailSendService.sendMail(emailMessage, "order/" + status.name().toLowerCase(), tplData);

            emailSent = true;
        }
        
        if (nextStatus != null) { // 상태 변경
            OrderStatusHistory history = OrderStatusHistory
                    .builder()
                    .prevStatus(prevStatus)
                    .status(status)
                    .orderSeq(orderSeq)
                    .emailSent(emailSent)
                    .build();

            orderStatusHistoryRepository.saveAndFlush(history);
        }
        
    }

    public void change(Long orderSeq, OrderStatus status) {
        change(orderSeq, null, status);
    }
}
