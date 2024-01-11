package org.choongang.api.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/map/api")
@RequiredArgsConstructor
public class MapApiController {

    private final Utils utils;

    /**
     * 클릭한 위치에 마커 표시하고 좌표 얻기
     *
     * @return
     */
    @GetMapping("/test1")
    public String index(Model model) {

        model.addAttribute("addCommonScript", new String[] { "map" });
        model.addAttribute("addScript", new String[] { "etc/test1"} );

        return utils.tpl("etc/map/test1");
    }
}
