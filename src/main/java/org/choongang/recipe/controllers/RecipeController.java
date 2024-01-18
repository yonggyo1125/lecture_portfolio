package org.choongang.recipe.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController implements ExceptionProcessor {
    private final Utils utils;

    @GetMapping("/write")
    public String write(@ModelAttribute RequestRecipe form, Model model) {
        commonProcess("add", model);
        return utils.tpl("recipe/add");
    }

    @PostMapping("/save")
    public String save(@Valid RequestRecipe form, Errors errors, Model model) {
        String mode = form.getMode();
        commonProcess(mode, model);

        if (errors.hasErrors()) {
            return utils.tpl("recipe/" + mode);
        }


        return "redirect:/recipe"; // 레서피 목록
    }

    private void commonProcess(String mode, Model model) {
        String pageTitle = Utils.getMessage("레서피", "commons");
        mode = StringUtils.hasText(mode) ? mode : "list";

        List<String> addCss = new ArrayList<>();
        List<String> addCommonScript = new ArrayList<>();
        List<String> addScript = new ArrayList<>();

        if (mode.equals("add") || model.equals("edit")) {
            addCss.add("recipe/style");
            addCommonScript.add("fileManager");
            addScript.add("recipe/form");
            pageTitle = Utils.getMessage("레서피_작성", "commons");
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("addCommonScript", addCommonScript);
        model.addAttribute("addScript", addScript);
        model.addAttribute("addCss", addCss);
    }
}
