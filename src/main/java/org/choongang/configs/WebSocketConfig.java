package org.choongang.configs;

import lombok.RequiredArgsConstructor;
import org.choongang.upbit.UpBitHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig  implements WebSocketConfigurer {

    private final UpBitHandler upBitHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(upBitHandler, "upbit")
                .setAllowedOrigins("*");
    }
}
