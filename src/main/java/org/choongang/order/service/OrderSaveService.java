package org.choongang.order.service;

import lombok.RequiredArgsConstructor;
import org.choongang.cart.service.CartInfoService;
import org.choongang.order.controllers.RequestOrder;
import org.choongang.order.repositories.OrderInfoRepository;
import org.choongang.order.repositories.OrderItemRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSaveService {

    private final CartInfoService cartInfoService;
    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;

    public void save(RequestOrder form) {



    }

}
