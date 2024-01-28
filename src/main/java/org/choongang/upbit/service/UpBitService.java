package org.choongang.upbit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.choongang.upbit.UpBitMarket;
import org.choongang.upbit.entities.QUpBitTicker;
import org.choongang.upbit.entities.UpBitTicker;
import org.choongang.upbit.repositories.UpBitTickerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Order.asc;

@Service
@RequiredArgsConstructor
public class UpBitService {

    private final UpBitTickerRepository repository;

    //@Scheduled(fixedDelay=1, timeUnit = TimeUnit.MINUTES)
    public void updateData() {
       RestTemplate restTemplate = new RestTemplate();
       String apiMarketUrl = "https://api.upbit.com/v1/market/all";

       String data = restTemplate.getForObject(apiMarketUrl, String.class);
       ObjectMapper om = new ObjectMapper();
       om.registerModule(new JavaTimeModule());
       try {
           List<UpBitMarket> markets = om.readValue(data, new TypeReference<>() {});

           Map<String, String[]> marketNames = markets.stream()
                   .collect(Collectors.toMap(UpBitMarket::getMarket,
                           m -> new String[] { m.getKorean_name(), m.getEnglish_name()}));

           String _markets = markets.stream().map(UpBitMarket::getMarket).collect(Collectors.joining(","));

           String apiTickerUrl = "https://api.upbit.com/v1/ticker?markets=" + _markets;
           String data2 = restTemplate.getForObject(apiTickerUrl, String.class);
           List<UpBitTicker> items = om.readValue(data2, new TypeReference<>() {});
           items.forEach(item -> item.setTradeDateTime(LocalDateTime.of(item.getTrade_date_kst(), item.getTrade_time_kst())));


           repository.saveAllAndFlush(items);

           /*
           List<UpBitTicker> upItems = new ArrayList<>(); // 수정, 추가되는 데이터
           for (UpBitTicker item : items) {
                String market = item.getMarket();

                UpBitTicker upItem = repository.findById(market).orElseGet(UpBitTicker::new);
                upItem.setMarket(item.getMarket());
                upItem.setTrade_date(item.getTrade_date());
                upItem.setTrade_time(item.getTrade_time());
                upItem.setTrade_date_kst(item.getTrade_date_kst());
                upItem.setTrade_time_kst(item.getTrade_time_kst());
                upItem.setTrade_timestamp(item.getTrade_timestamp());
                upItem.setOpening_price(item.getOpening_price());
                upItem.setHigh_price(item.getHigh_price());
                upItem.setLow_price(item.getLow_price());
                upItem.setTrade_price(item.getTrade_price());
                upItem.setPrev_closing_price(item.getPrev_closing_price());
                upItem.setChange(item.getChange());
                upItem.setChange_price(item.getChange_price());
                upItem.setChange_rate(item.getChange_rate());
                upItem.setSigned_change_price(item.getSigned_change_price());
                upItem.setSigned_change_rate(item.getSigned_change_rate());
                upItem.setTrade_volume(item.getTrade_volume());
                upItem.setAcc_trade_price(item.getAcc_trade_price());
                upItem.setAcc_trade_price_24h(item.getAcc_trade_price_24h());
                upItem.setAcc_trade_volume(item.getTrade_volume());
                upItem.setAcc_trade_volume_24h(item.getAcc_trade_volume_24h());
                upItem.setHighest_52_week_price(item.getHighest_52_week_price());
                upItem.setHighest_52_week_date(item.getHighest_52_week_date());
                upItem.setLowest_52_week_date(item.getLowest_52_week_date());
                upItem.setLowest_52_week_price(item.getLowest_52_week_price());
                upItem.setTimestamp(item.getTimestamp());

                String[] names = marketNames.get(item.getMarket());
                upItem.setKorean_name(names[0]);
                upItem.setEnglish_name(names[1]);

                upItems.add(upItem);
           } */



       } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
       }
   }

   public List<UpBitTicker> getList(UpBitTickerSearch search) {
       QUpBitTicker upBitTicker = QUpBitTicker.upBitTicker;

       List<String> markets = search.getMarkets();
       long interval = search.getInterval();

       interval = interval < 60L ? 60L : interval;
       LocalDateTime dateTime = LocalDateTime.now().minusSeconds(interval);


       BooleanBuilder builder = new BooleanBuilder();
       builder.and(upBitTicker.tradeDateTime.goe(dateTime));

       if (markets != null && !markets.isEmpty()) {
           builder.and(upBitTicker.market.in(markets));
       }

       List<UpBitTicker> items = (List<UpBitTicker>)repository.findAll(builder, Sort.by(asc("tradeDateTime")));

       return items;
   }
}
