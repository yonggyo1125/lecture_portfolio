package org.choongang.upbitapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.choongang.upbit.UpBitMarket;
import org.choongang.upbit.entities.UpBitTicker;
import org.choongang.upbit.service.UpBitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class ApiTest {

    @Autowired
    private UpBitService service;

    @Test
    void test1() throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://api.upbit.com/v1/market/all";
        String data = restTemplate.getForObject(url, String.class);

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());

        List<UpBitMarket> markets = om.readValue(data, new TypeReference<>() {});

        String _markets = markets.stream().map(UpBitMarket::getMarket).collect(Collectors.joining(","));
       // System.out.println(_markets);

        // 시세 정보
        String url2 = "https://api.upbit.com/v1/ticker?markets=" + _markets;
        String data2 = restTemplate.getForObject(url2, String.class);

        List<UpBitTicker> items = om.readValue(data2, new TypeReference<>() {});

        items.forEach(System.out::println);

    }

    @Test
    void test2() {
        service.updateData();
    }
}
