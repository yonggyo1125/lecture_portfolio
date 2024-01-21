# 주문

## 상수 정의

> order/constants/PayType.java

```java
package org.choongang.order.constants;

/**
 * 결제 수단
 *
 */
public enum PayType {
    LBT, // 무통장 입금
    VACCOUNT, // 가상계좌
    CARD, // 신용카드
    DIRECT // 계좌이체
}
```


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

## 커맨드 객체 정의

> order/controllers/RequestOrder.java

```java
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
```

> resources/messages/validations.properties

```properties
# 공통 
NotBlank=필수입력항목
Email=이메일 형식이 아닙니다.

# 회원
NotBlank.email=이메일을 입력하세요.
NotBlank.userId=아이디를 입력하세요.
NotBlank.password=비밀번호를 입력하세요.
NotBlank.confirmPassword=비밀번호를 확인하세요.
NotBlank.requestJoin.name=회원명을 입력하세요.
AssertTrue.requestJoin.agree=회원가입 약관에 동의하세요.
Size.requestJoin.userId=아이디는 6자리 이상 입력하세요.
Size.requestJoin.password=비밀번호는 8자리 이상 입력하세요.
Duplicated.requestJoin.userId=이미 가입된 아이디 입니다.
Duplicated.requestJoin.email=이미 가입된 이메일 입니다.
Complexity.requestJoin.password=비밀번호는 대소문자 각각 알파벳 1자 이상, 숫자 1자 이상, 특수문자 1자 이상을 포함해야 합니다.
Mismatch.password=비밀번호가 일치하지 않습니다.
Required.verified.email=이메일 인증이 필요합니다.

# 비밀번호 찾기
NotBlank.requestFindPw.name=회원명을 입력하세요.

# 관리자 - 상품 - 카테고리
NotBlank.requestCategory.cateCd=분류코드를 입력하세요.
NotBlank.requestCategory.cateNm=분류명을 입력하세요.
Duplicated.requestCategory.cateCd=이미 등록된 분류코드 입니다.

#관리자 - 상품 - 등록/수정
NotBlank.requestProduct.cateCd=분류코드를 선택하세요.
NotBlank.requestProduct.name=상품명을 입력하세요.

# 사용자 - 장바구니
상품을_선택_하세요.=상품을 선택 하세요.

# 주문서
NotBlank.requestOrder.orderName=주문자명을 입력하세요.
NotBlank.requestOrder.orderCellPhone=주문자 휴대전화번호를 입력하세요.
NotBlank.requestOrder.orderEmail=주문자 이메일을 입력하세요.
NotBlank.requestOrder.receiverName=받는분 이름을 입력하세요.
NotBlank.requestOrder.receiverCellPhone=받는분 휴대전화 번호를 입력하세요.
NotBlank.requestOrder.zonecode=우편번호를 입력하세요.
NotBlank.requestOrder.address=주소를 입력하세요.
NotBlank.requestOrder.payType=결제 수단을 선택하세요.
```

## 엔티티 및 레포지토리 구성

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
import org.choongang.order.constants.PayType;

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

> order/repositories/OrderInfoRepository.java

```java
package org.choongang.order.repositories;

import org.choongang.order.entities.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long>, QuerydslPredicateExecutor<OrderInfo> {
}

```

> order/repositories/OrderItemRepository.jav

```java
package org.choongang.order.repositories;

import org.choongang.order.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, QuerydslPredicateExecutor<OrderItem> {
    
}
```
## 
