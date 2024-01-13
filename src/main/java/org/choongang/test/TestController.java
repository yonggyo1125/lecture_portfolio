package org.choongang.test;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController implements ExceptionProcessor {

    private final Utils utils;

    @GetMapping("/popup")
    public String popupTest() {

        return utils.tpl("test/popup");
    }
}
