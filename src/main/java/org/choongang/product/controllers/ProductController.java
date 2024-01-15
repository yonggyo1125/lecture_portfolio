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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController implements ExceptionProcessor {

    private final CategoryInfoService categoryInfoService;
    private final ProductInfoService productInfoService;
    private final Utils utils;

    private Category category; // 상품 분류


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
        Product product = productInfoService.get(seq);

        model.addAttribute("product", product);

        return utils.tpl("product/detail");
    }

    /**
     * 상품 공통 처리
     *
     * @param cateCd : 분류 코드
     * @param mode
     * @param model
     */
    private void commonProcess(String cateCd, String mode, Model model) {
        category = categoryInfoService.get(cateCd);
        String pageTitle = category.getCateNm();



        model.addAttribute("category", category);
        model.addAttribute("pageTitle", pageTitle);
    }
}
