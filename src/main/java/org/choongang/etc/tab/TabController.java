package org.choongang.etc.tab;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/etc/tab")
@RequiredArgsConstructor
public class TabController {

    private final Utils utils;

    @GetMapping
    public String index(Model model) {

        model.addAttribute("addCommonScript", new String[] {"tab"});
        model.addAttribute("addCommonCss", new String[] { "tab"});

        return utils.tpl("etc/tab/index");
    }

    @GetMapping("/content/{num}")
    public String content(@PathVariable("num") Long num) {
        return utils.tpl("etc/tab/content/" + num);
    }
}
