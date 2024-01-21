# 주문

## 공통

> order/constants/OrderStatus.java : 주문 상태 

```java
package org.choongang.order.constants;

public enum OrderStatus {
    READY, // 주문 접수 전
    ORDER, // 주문 접수
    IN_CASH, // 입금 확인
    PREPARE, // 상품 준비중
    DELIVERY, // 배송중
    ARRIVAL, // 배송 완료
    DONE, // 주문 완료
    CANCEL, // 입금확인 전 취소
    REFUND, // 입금 후 취소
    EXCHANGE, // 교환
}
```

## 엔티티 구성

> order/entities/OrderInfo.java : 주문 정보

```java
package org.choongang.order.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.commons.entities.Base;
import org.choongang.member.entities.Member;
import org.choongang.order.constants.OrderStatus;

@Data
@Builder
@Entity
@NoArgsConstructor @AllArgsConstructor
public class OrderInfo extends Base {

    @Id
    @GeneratedValue
    private Long seq;

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

    @Column(length=100, nullable = false)
    private String addressSub; // 배송주소 - 나머지 주소

    @Column(length=150, nullable = false)
    private String deliveryMemo; // 배송 메모

    private int totalPrice; // 주문 시점 상품 합계
    private int totalDeliveryPrice; // 주문 시점 배송비
    private int totalDiscount; // 주문 시점 총 할인 금액
    private int payPrice; // 주문 시점 결제 금액
}
```

> order/entities/OrderItem.java : 주문 상품 

```java
package org.choongang.order.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.commons.entities.Base;
import org.choongang.member.entities.Member;
import org.choongang.order.constants.OrderStatus;
import org.choongang.product.entities.Product;
import org.choongang.product.entities.ProductOption;


@Data
@Builder
@Entity
@NoArgsConstructor @AllArgsConstructor
public class OrderItem extends Base {
    @Id
    @GeneratedValue
    private Long seq; // 품주번호

    @Enumerated(EnumType.STRING)
    @Column(length=30, nullable = false)
    private OrderStatus status = OrderStatus.READY; // 주문 접수전 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="orderInfoSeq")
    private OrderInfo orderInfo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="productSeq")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="productOptionSeq")
    private ProductOption option;

    private int ea = 1; // 주문수량

    @Column(length=150, nullable = false)
    private String productName; // 주문 시점의 상품 명
    private String optionName; // 주문 시점의 옵션 항목
    private String optionValue; // 주문 시점의 선택한 옵션 값

    private int salePrice; // 주문 시점의 상품 판매가

    private int totalPrice; // 주문 시점의 상품 합계
    private int totalDiscount; // 주문 시점의 총 할인가

    @Column(length=60)
    private String deliveryCompany; // 배송 업체
    @Column(length=60)
    private String deliveryInvoice; // 운송장 번호
}
```


