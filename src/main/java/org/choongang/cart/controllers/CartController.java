package org.choongang.cart.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.ExceptionProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController implements ExceptionProcessor {

    /**
     * 장바구니에 상품 등록
     *      mode : cart - 장바구니 페이지 노출 상품
     *             direct - 바로 구매 상품
     * @return
     */
    @PostMapping("/save")
    public String save(Model model) {


        return "common/_execute_script";
    }
}
