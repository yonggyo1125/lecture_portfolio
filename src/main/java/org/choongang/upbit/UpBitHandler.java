package org.choongang.upbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.choongang.upbit.entities.UpBitTicker;
import org.choongang.upbit.service.UpBitService;
import org.choongang.upbit.service.UpBitTickerSearch;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UpBitHandler extends TextWebSocketHandler {

    private final UpBitService service;

    private static List<WebSocketSession> sessions = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        UpBitTickerSearch search = new UpBitTickerSearch();
        search.setInterval(300L);
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        while(true) {
            List<UpBitTicker> items = service.getList(search);
            String data = om.writeValueAsString(items);
            TextMessage jsonData = new TextMessage(data);
            for (WebSocketSession s : sessions) {
                s.sendMessage(jsonData);
            }

            Thread.sleep(300000);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}
