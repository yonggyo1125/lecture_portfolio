package org.choongang.order.service;

import lombok.RequiredArgsConstructor;
import org.choongang.cart.entities.CartInfo;
import org.choongang.cart.service.CartData;
import org.choongang.cart.service.CartInfoService;
import org.choongang.order.controllers.RequestOrder;
import org.choongang.order.repositories.OrderInfoRepository;
import org.choongang.order.repositories.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderSaveService {

    private final CartInfoService cartInfoService;
    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;

    public void save(RequestOrder form) {
        /** 장바구니에서 상품 정보 가져오기 */
        List<Long> cartSeqs = form.getCartSeq();
        CartData cartData = cartInfoService.getCartInfo(cartSeqs);

        List<CartInfo> cartItems = cartData.getItems();
        int totalPrice = cartData.getTotalPrice(); // 상품가 합계
        int totalDiscount = cartData.getTotalDiscount(); // 할인금액 합계
        int totalDeliveryPrice = cartData.getTotalDeliveryPrice(); // 배송비 합계
        int payPrice = cartData.getPayPrice(); // 결제금액 합계 - 상품가 - 할인금액 + 배송비


    }

}
