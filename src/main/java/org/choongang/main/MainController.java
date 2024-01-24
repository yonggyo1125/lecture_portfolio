package org.choongang.main;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {

    private final Utils utils;

    @GetMapping
    public String index() {

        return utils.tpl("main/index");
    }
}
