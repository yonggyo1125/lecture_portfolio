package org.choongang.order.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.commons.entities.Base;
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
