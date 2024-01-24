package org.choongang.upbit.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UpBitTicker {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long seq;

    @Column(length=30)
    private String market;

    @JsonIgnore
    private String korean_name;

    @JsonIgnore
    private String english_name;

    @JsonFormat(pattern="yyyyMMdd")
    private LocalDate trade_date;

    @JsonFormat(pattern="HHmmss")
    private LocalTime trade_time;

    @JsonFormat(pattern="yyyyMMdd")
    private LocalDate trade_date_kst;

    @JsonFormat(pattern="HHmmss")
    private LocalTime trade_time_kst;

    @JsonIgnore
    private LocalDateTime tradeDateTime;

    private Long trade_timestamp;

    private Long opening_price;

    private Long high_price;

    private Long low_price;

    private Long trade_price;

    private Long prev_closing_price;

    @Column(length=30)
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
