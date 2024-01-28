package org.choongang.main;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {

    private final Utils utils;

    @GetMapping
    public String index(Model model) {

        model.addAttribute("addCommonScript", new String[] {"upbit"});

        return utils.tpl("main/index3");
    }
}
