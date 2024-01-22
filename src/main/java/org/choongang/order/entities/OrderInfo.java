package org.choongang.order.entities;

import jakarta.persistence.*;
import lombok.*;
import org.choongang.commons.entities.Base;
import org.choongang.member.entities.Member;
import org.choongang.order.constants.OrderStatus;
import org.choongang.order.constants.PayType;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor @AllArgsConstructor
public class OrderInfo extends Base {

    @Id
    private Long seq = System.currentTimeMillis();

    /**
     * 이 주문 상태값은 각 품주별 주문상태 한개라도 특정 단계 이전이라면 이전단계에 맞춘다. 
     * 즉, 하위 주문 상태 지향
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private OrderStatus status = OrderStatus.READY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="memberSeq")
    private Member member;

    @Column(length=40, nullable = false)
    private String orderName; // 주문자명

    @Column(length=15, nullable = false)
    private String orderCellPhone; // 주문자 휴대전화번호

    @Column(length=90, nullable = false)
    private String orderEmail; // 주문자 이메일 정보

    @Column(length=40, nullable = false)
    private String receiverName; // 받는분 이름

    @Column(length=15, nullable = false)
    private String receiverCellPhone; // 받는분 휴대전화 버놓

    @Column(length=15, nullable = false)
    private String zonecode; // 배송주소 - 우편번호

    @Column(length=100, nullable=false)
    private String address; // 배송주소 - 주소

    @Column(length=100)
    private String addressSub; // 배송주소 - 나머지 주소

    @Column(length=150, nullable = false)
    private String deliveryMemo; // 배송 메모

    private int totalPrice; // 주문 시점 상품 합계
    private int totalDeliveryPrice; // 주문 시점 배송비
    private int totalDiscount; // 주문 시점 총 할인 금액
    private int payPrice; // 주문 시점 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PayType payType; // 결제 수단

    private String depositor; // 무통장 입금일 경우 입금자명

    @ToString.Exclude
    @OneToMany(mappedBy = "orderInfo", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();
}
