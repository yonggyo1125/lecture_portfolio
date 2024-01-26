package org.choongang.order.service;

import lombok.RequiredArgsConstructor;
import org.choongang.cart.entities.CartInfo;
import org.choongang.cart.service.CartData;
import org.choongang.cart.service.CartInfoService;
import org.choongang.member.MemberUtil;
import org.choongang.order.constants.OrderStatus;
import org.choongang.order.constants.PayType;
import org.choongang.order.controllers.RequestOrder;
import org.choongang.order.entities.OrderInfo;
import org.choongang.order.entities.OrderItem;
import org.choongang.order.repositories.OrderInfoRepository;
import org.choongang.order.repositories.OrderItemRepository;
import org.choongang.product.entities.Product;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderSaveService {

    private final CartInfoService cartInfoService;
    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberUtil memberUtil;

    public OrderInfo save(RequestOrder form) {
        /** 장바구니에서 상품 정보 가져오기 */
        List<Long> cartSeqs = form.getCartSeq();
        CartData cartData = cartInfoService.getCartInfo(cartSeqs);

        List<CartInfo> cartItems = cartData.getItems();
        int totalPrice = cartData.getTotalPrice(); // 상품가 합계
        int totalDiscount = cartData.getTotalDiscount(); // 할인금액 합계
        int totalDeliveryPrice = cartData.getTotalDeliveryPrice(); // 배송비 합계
        int payPrice = cartData.getPayPrice(); // 결제금액 합계 - 상품가 - 할인금액 + 배송비

        /* 주문 정보 저장 S */
        OrderInfo orderInfo = new ModelMapper().map(form, OrderInfo.class);
        orderInfo.setStatus(OrderStatus.READY);
        orderInfo.setPayType(PayType.valueOf(form.getPayType()));
        orderInfo.setTotalPrice(totalPrice);
        orderInfo.setTotalDiscount(totalDiscount);
        orderInfo.setTotalDeliveryPrice(totalDeliveryPrice);
        orderInfo.setPayPrice(payPrice);

        orderInfo.setMember(memberUtil.getMember());
        orderInfo.setOrderName(form.getOrderName());
        orderInfo.setOrderCellPhone(form.getOrderCellPhone());
        orderInfo.setOrderEmail(form.getOrderEmail());
        orderInfo.setReceiverName(form.getReceiverName());
        orderInfo.setReceiverCellPhone(form.getReceiverCellPhone());
        orderInfo.setZonecode(form.getZonecode());
        orderInfo.setAddress(form.getAddress());
        orderInfo.setAddressSub(form.getAddressSub());
        orderInfo.setDeliveryMemo(form.getDeliveryMemo());
        orderInfo.setDepositor(form.getDepositor());

        orderInfoRepository.saveAndFlush(orderInfo);
        /* 주문 정보 저장 E */

        /* 주문 상품 정보 저장 S */
        List<OrderItem> items = new ArrayList<>();
        for (CartInfo cartItem : cartItems) {
            Product product = cartItem.getProduct();
            OrderItem item = OrderItem.builder()
                    .status(OrderStatus.READY)
                    .product(product)
                    .optionName(product.getOptionName())
                    .productName(product.getName())
                    .ea(cartItem.getEa())
                    .salePrice(product.getSalePrice())
                    .totalDiscount(cartItem.getTotalDiscount())
                    .totalPrice(cartItem.getTotalPrice())
                    .orderInfo(orderInfo)
                    .build();
            items.add(item);
        }
        orderItemRepository.saveAllAndFlush(items);
        /* 주문 상품 정보 저장 E */

        return orderInfo;
    }

}
