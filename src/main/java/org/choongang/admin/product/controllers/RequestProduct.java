package org.choongang.admin.product.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.choongang.file.entities.FileInfo;
import org.choongang.product.constants.DiscountType;
import org.choongang.product.constants.ProductStatus;

import java.util.List;
import java.util.UUID;

@Data
public class RequestProduct {

    private String mode = "add";

    private Long seq; // 상품 번호

    private String gid = UUID.randomUUID().toString(); // 그룹 ID

    @NotBlank
    private String cateCd; // 분류 코드

    @NotBlank
    private String name; // 상품명

    private int consumerPrice; // 소비자가(보이는 금액)
    private int salePrice; // 판매가(결제 기준 금액)

    private boolean useStock; // 재고 사용 여부 - true : 재고 차감
    private int stock; // 옵션을 사용하지 않는 경우 단일 상품 재고, 0 - 무제한

    private String discountType = DiscountType.PERCENT.name(); // 할인 종류 - PERCENT, PRICE
    private int discount; // 할인 금액

    private String extraInfo; // 상품 추가 정보 : JSON 문자열로 저장 - 양식에서는 hidden 값으로 업데이트 처리

    private boolean packageDelivery; // 같은 판매자별 묶음 배송 여부
    private int deliveryPrice; // 배송비, 0이면 무료 배송

    private String description; // 상품 상세 설명

    private boolean active; // 노출 여부 : true -> 소비자 페이지 노출

    private String status = ProductStatus.PREPARE.name(); // 상품 상태 - 상품 준비중이 기본 상태

    private boolean useOption; // 옵션 사용 여부, true : 옵션 사용, 재고는 옵션쪽 재고 사용

    private String optionName; // 옵션명

    private List<FileInfo> mainImages; // 메인 이미지

    private List<FileInfo> listImages; // 목록 이미지

    private List<FileInfo> editorImages; // 에디터에 첨부한 이미지
}
