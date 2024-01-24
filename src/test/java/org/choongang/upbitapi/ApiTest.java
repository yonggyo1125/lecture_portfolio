package org.choongang.upbitapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.choongang.upbit.UpBitMarket;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class ApiTest {

    @Test
    void test1() throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://api.upbit.com/v1/market/all";
        String data = restTemplate.getForObject(url, String.class);

        ObjectMapper om = new ObjectMapper();
        List<UpBitMarket> markets = om.readValue(data, new TypeReference<>() {});

        String _markets = markets.stream().map(UpBitMarket::getMarket).collect(Collectors.joining(","));
       // System.out.println(_markets);

        // 시세 정보
        String url2 = "https://api.upbit.com/v1/ticker?markets=" + _markets;
        String data2 = restTemplate.getForObject(url2, String.class);



    }
}
