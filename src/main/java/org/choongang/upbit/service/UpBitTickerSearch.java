package org.choongang.upbit.service;

import lombok.Data;

import java.util.List;

@Data
public class UpBitTickerSearch {
    private List<String> markets;
    private long interval; // 초단위
}
