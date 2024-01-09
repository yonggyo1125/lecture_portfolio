package org.choongang.admin.config.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.admin.config.service.ConfigInfoService;
import org.choongang.admin.config.service.ConfigSaveService;
import org.choongang.admin.menus.Menu;
import org.choongang.admin.menus.MenuDetail;
import org.choongang.commons.ExceptionProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/config/api")
@RequiredArgsConstructor
public class ApiConfigController  implements ExceptionProcessor {

    private final ConfigSaveService saveService;
    private final ConfigInfoService infoService;

    @ModelAttribute("menuCode")
    public String getMenuCode() {
        return "config";
    }

    @ModelAttribute("subMenuCode")
    public String getSubMenuCode() {
        return "api";
    }

    @ModelAttribute("subMenus")
    public List<MenuDetail> getSubMenus() {
        return Menu.getMenus("config");
    }

    @ModelAttribute("pageTitle")
    public String getPageTitle() {
        return "API 설정";
    }

    @GetMapping
    public String index(Model model) {

        ApiConfig config = infoService.get("apiConfig", ApiConfig.class).orElseGet(ApiConfig::new);

        model.addAttribute("apiConfig", config);

        return "admin/config/api";
    }

    @PostMapping
    public String save(ApiConfig config, Model model) {

        saveService.save("apiConfig", config);

        model.addAttribute("message", "저장되었습니다.");

        return "admin/config/api";
    }

}
