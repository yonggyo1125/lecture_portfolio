package org.choongang.cart.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.cart.constants.CartType;
import org.choongang.cart.controllers.RequestCart;
import org.choongang.cart.entities.CartInfo;
import org.choongang.cart.repositories.CartInfoRepository;
import org.choongang.commons.Utils;
import org.choongang.member.MemberUtil;
import org.choongang.member.entities.Member;
import org.choongang.product.entities.Product;
import org.choongang.product.service.ProductInfoService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartSaveService {

    private final ProductInfoService productInfoService;
    private final CartInfoRepository cartInfoRepository;
    private final HttpServletRequest request;
    private final MemberUtil memberUtil;
    private final Utils utils;

    public void save(RequestCart form) {

        Long seq = form.getSeq(); // 상품 번호
        String mode = form.getMode(); //
        int uid = utils.cartUid();
        Member member = memberUtil.getMember();
        Product product = productInfoService.get(seq); // 상품 엔티티

        List<Integer> nums = form.getSelectedNums();

        List<CartInfo> items = new ArrayList<>();
        for (int num : nums) {

            int ea = Integer.parseInt(utils.getParam("ea_" + num));

            CartInfo item = CartInfo.builder()
                    .mode(CartType.valueOf(mode))
                    .product(product)
                    .uid(uid)
                    .ea(ea)
                    .member(member)
                    .build();

            items.add(item);
        }

        cartInfoRepository.saveAllAndFlush(items);
    }
}
