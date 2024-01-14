package org.choongang.product.controllers;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductSearch {
    private int page = 1;
    private int limit = 20;

    private List<String> cateCd; // 카테고리 검색
    private List<Long> seq; // 상품 번호
    private String name; // 상품 명

    private List<String> statuses; // 상품 상태

    private LocalDate sdate; // 날짜 검색 시작
    private LocalDate edate; // 날짜 검색 종료
}
