package org.choongang.order.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.cart.constants.CartType;
import org.choongang.cart.service.CartData;
import org.choongang.cart.service.CartInfoService;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@SessionAttributes({"cartData", "mode"}) // model.addAttribute -> 세션쪽에도 자동 추가
public class OrderController implements ExceptionProcessor {

    private final CartInfoService cartInfoService;
    private final Utils utils;

    /**
     * 주문서 작성
     *
     * @param seq : 장바구니 등록 번호
     *              장바구니 등록번호가 없으면 바로구매 : CartType.DIRECT
     *                  바로구매(DIRECT) : 상품 상세에서 바로 주문하는 경우
     *                  CART : 장바구니 -> 주문하기
     * @param model
     * @return
     */
    @GetMapping
    public String order(@RequestParam(name="seq", required = false) List<Long> seq, @ModelAttribute RequestOrder form, Model model) {
        commonProcess("order", model);

        CartType mode = seq == null || seq.isEmpty() ? CartType.DIRECT : CartType.CART;
        CartData data = cartInfoService.getCartInfo(mode, seq);


        model.addAttribute("cartData", data);

        return utils.tpl("order/order_form");
    }

    @PostMapping
    public String orderPs(@Valid RequestOrder form, Errors errors, Model model, SessionStatus status) {
        commonProcess("order", model);

        if (errors.hasErrors()) {
            return utils.tpl("order/order_form");
        }



        status.setComplete(); // cartData 세션 비우기

        return "redirect:/order/end/주문번호";
    }

    /**
     * 주문 공통 처리
     *
     * @param mode
     * @param model
     */
    private void commonProcess(String mode, Model model) {

        model.addAttribute("mode", mode);
    }
}
