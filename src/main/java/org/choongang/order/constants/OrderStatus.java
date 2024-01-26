package org.choongang.order.constants;

public enum OrderStatus {
    READY(false), // 주문 접수 전
    ORDER(true), // 주문 접수
    IN_CASH(true), // 입금 확인
    PREPARE(false), // 상품 준비중
    DELIVERY(true), // 배송중
    ARRIVAL(false), // 배송 완료
    DONE(true), // 주문 완료
    CANCEL(true), // 입금확인 전 취소
    REFUND(true), // 입금 후 취소
    EXCHANGE(true); // 교환

    private final boolean emailStatus; // 이메일 전송 주문 단계 여부

    OrderStatus(boolean emailStatus) {
        this.emailStatus = emailStatus;
    }

    public boolean getEmailStatus() {
        return emailStatus;
    }
}
