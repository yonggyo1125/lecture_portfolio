package org.choongang.order.controllers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.choongang.order.constants.PayType;

import java.util.List;

@Data
public class RequestOrder {

    private List<Long> cartSeq; // 장바구니 등록 번호
    
    private String mode = "DIRECT"; // DIRECT : 바로 주문, CART : 장바구니에서 주문한 상품
    
    @NotBlank
    private String orderName; // 주문자명

    @NotBlank
    private String orderCellPhone; // 주문자 휴대전화번호

    @Email
    @NotBlank
    private String orderEmail; // 주문자 이메일 정보

    @NotBlank
    private String receiverName; // 받는분 이름

    @NotBlank
    private String receiverCellPhone; // 받는분 휴대전화 버놓

    @NotBlank
    private String zonecode; // 배송주소 - 우편번호

    @NotBlank
    private String address; // 배송주소 - 주소

    private String addressSub; // 배송주소 - 나머지 주소

    private String deliveryMemo; // 배송 메모

    @NotBlank
    private String payType = PayType.LBT.name(); // 결제 수단

    private String depositor; // 무통장 입금일 경우 입금자명

}
