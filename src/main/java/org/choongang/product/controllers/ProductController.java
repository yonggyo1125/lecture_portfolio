package org.choongang.product.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.ListData;
import org.choongang.commons.Utils;
import org.choongang.product.entities.Category;
import org.choongang.product.entities.Product;
import org.choongang.product.service.CategoryInfoService;
import org.choongang.product.service.ProductInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController implements ExceptionProcessor {

    private final CategoryInfoService categoryInfoService;
    private final ProductInfoService productInfoService;
    private final Utils utils;

    private Category category; // 상품 분류
    private Product product; // 상품

    @GetMapping("/{cateCd}")
    public String list(@PathVariable("cateCd") String cateCd, ProductSearch search, Model model) {
        commonProcess(cateCd, "list", model);

        ListData<Product> data = productInfoService.getList(search, false);

        model.addAttribute("items", data.getItems());
        model.addAttribute("pagination", data.getPagination());

        return utils.tpl("product/list");
    }

    @GetMapping("/detail/{seq}")
    public String detail(@PathVariable("seq") Long seq, Model model) {
        commonProcess(seq, "detail", model);

        return utils.tpl("product/view");
    }

    /**
     * 상품 공통 처리
     *
     * @param cateCd : 분류 코드 - 상품 목록
     * @param mode
     * @param model
     */
    private void commonProcess(String cateCd, String mode, Model model) {
        category = categoryInfoService.get(cateCd);
        String pageTitle = category.getCateNm();

        mode = StringUtils.hasText(mode) ? mode : "list";

        List<String> addCss = new ArrayList<>();
        List<String> addScript = new ArrayList<>();

        addCss.add("product/style"); // 상품 공통
        if (mode.equals("detail")) { // 상품 상세
            addScript.add("product/detail");

            if (product != null) pageTitle = product.getName(); // 상품 상세인 경우 상품명으로 제목 표기
        }
  
        model.addAttribute("addCss", addCss);
        model.addAttribute("addScript", addScript);
        model.addAttribute("category", category);
        model.addAttribute("pageTitle", pageTitle);
    }

    /**
     * 상품 공통 처리 - 상품 상세
     *
     * @param seq : 상품 번호
     * @param mode
     * @param model
     */
    private void commonProcess(Long seq, String mode, Model model) {
        product = productInfoService.get(seq);
        Category category = product.getCategory();
        String cateCd = category == null ? null : category.getCateCd();

        model.addAttribute("product", product);
        commonProcess(cateCd, mode, model);
    }
}
