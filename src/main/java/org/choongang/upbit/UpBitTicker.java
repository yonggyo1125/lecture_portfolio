package org.choongang.upbit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpBitTicker {
    private String market;

    @JsonFormat(pattern="yyyyMMdd")
    private LocalDate trade_date;

    @JsonFormat(pattern="HHmmss")
    private LocalTime trade_time;

    @JsonFormat(pattern="yyyyMMdd")
    private LocalDate trade_date_kst;

    @JsonFormat(pattern="HHmmss")
    private LocalTime trade_time_kst;

    private Long trade_timestamp;

    private Long opening_price;

    private Long high_price;

    private Long low_price;

    private Long trade_price;

    private Long prev_closing_price;

    private String change;

    private Long change_price;

    private Double change_rate;

    private Long signed_change_price;

    private Double signed_change_rate;

    private Double trade_volume;

    private Double acc_trade_price;
    private Double acc_trade_price_24h;
    private Double acc_trade_volume;
    private Double acc_trade_volume_24h;
    private Long highest_52_week_price;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate highest_52_week_date;

    private Long lowest_52_week_price;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate lowest_52_week_date;

    private Long timestamp;
}
