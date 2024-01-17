package org.choongang.admin.product.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.menus.Menu;
import org.choongang.admin.menus.MenuDetail;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.ListData;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.choongang.file.service.FileInfoService;
import org.choongang.product.constants.ProductStatus;
import org.choongang.product.controllers.ProductSearch;
import org.choongang.product.entities.Category;
import org.choongang.product.entities.Product;
import org.choongang.product.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller("adminProductController")
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class ProductController implements ExceptionProcessor {

    private final CategoryValidator categoryValidator;
    private final CategorySaveService categorySaveService;
    private final CategoryInfoService categoryInfoService;
    private final CategoryDeleteService categoryDeleteService;

    private final FileInfoService fileInfoService;
    private final ProductSaveService productSaveService;
    private final ProductInfoService productInfoService;
    private final ProductDeleteService productDeleteService;


    @ModelAttribute("menuCode")
    public String getMenuCode() {
        return "product";
    }

    @ModelAttribute("subMenus")
    public List<MenuDetail> getSubMenus() {
        return Menu.getMenus("product");
    }

    // 상품 상태 목록
    @ModelAttribute("productStatuses")
    public List<String[]> getProductStatuses() {
        return ProductStatus.getList();
    }

    
    // 상품 분류 목록
    @ModelAttribute("categories")
    public List<Category> getCategories() {
        return categoryInfoService.getList(true);
    }

    /**
     * 상품 목록
     *
     * @return
     */
    @GetMapping
    public String list(ProductSearch form, Model model) {
        commonProcess("list", model);

        ListData<Product> data = productInfoService.getList(form, true);

        model.addAttribute("items", data.getItems());
        model.addAttribute("pagination", data.getPagination());

        return "admin/product/list";
    }

    /**
     * 상품 목록에서 수정
     * 
     * @param chks : 선택 번호
     * @param model
     * @return
     */
    @PatchMapping
    public String editList(@ModelAttribute("chk") List<Integer> chks, Model model) {
        commonProcess("list", model);

        productSaveService.saveList(chks);

        // 수정 후 새로고침
        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }

    /**
     * 상품 목록에서 삭제
     * 
     * @param chks : 선택 번호
     * @param model
     * @return
     */
    @DeleteMapping
    public String deleteList(@ModelAttribute("chk") List<Integer> chks, Model model) {
        commonProcess("list", model);

        productDeleteService.deleteList(chks);

        // 삭제 후 새로고침
        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }

    /**
     * 상품 등록
     *
     * @param model
     * @return
     */
    @GetMapping("/add")
    public String add(@ModelAttribute RequestProduct form, Model model) {
        commonProcess("add", model);

        return "admin/product/add";
    }


    /**
     * 상품 등록, 수정 처리
     *
     * @param model
     * @return
     */
    @PostMapping("/save")
    public String save(@Valid RequestProduct form, Errors errors, Model model) {
        String mode = form.getMode();
        commonProcess(mode, model);

        if (errors.hasErrors()) {

            String gid = form.getGid();

            form.setEditorImages(fileInfoService.getList(gid, "editor"));
            form.setMainImages(fileInfoService.getList(gid, "main"));
            form.setListImages(fileInfoService.getList(gid, "list"));

            return "admin/product/" + mode;
        }

        productSaveService.save(form);

        return "redirect:/admin/product";
    }

    /**
     * 상품 정보 수정
     *
     * @param seq
     * @param model
     * @return
     */
    @GetMapping("/edit/{seq}")
    public String edit(@PathVariable("seq") Long seq, Model model) {
        commonProcess("edit", model);

        RequestProduct form = productInfoService.getForm(seq);
        System.out.println(form);
        model.addAttribute("requestProduct", form);

        return "admin/product/edit";
    }

    /**
     * 상품 분류
     *
     * @param model
     * @return
     */
    @GetMapping("/category")
    public String category(@ModelAttribute RequestCategory form, Model model) {
        commonProcess("category", model);

        List<Category> items = categoryInfoService.getList(true);
        model.addAttribute("items", items);

        return "admin/product/category";
    }

    /**
     * 상품 분류 등록
     *
     * @param model
     * @return
     */
    @PostMapping("/category")
    public String categoryPs(@Valid RequestCategory form, Errors errors, Model model) {
        commonProcess("category", model);

        categoryValidator.validate(form, errors);

        if (errors.hasErrors()) {
            List<String> messages = errors.getFieldErrors()
                    .stream()
                    .map(e -> e.getCodes())
                    .map(s -> Utils.getMessage(s[0]))
                    .toList();

            throw new AlertException(messages.get(0), HttpStatus.BAD_REQUEST);
        }

        categorySaveService.save(form);

        // 분류 추가가 완료되면 부모창 새로고침
        model.addAttribute("script", "parent.location.reload()");
        return "common/_execute_script";
    }

    /**
     * 분류 수정
     *
     * @return
     */
    @PatchMapping("/category")
    public String categoryEdit(@RequestParam("chk") List<Integer> chks, Model model) {
        commonProcess("category", model);

        categorySaveService.saveList(chks);

        // 수정 완료 -> 목록 갱신
        model.addAttribute("script", "parent.location.reload()");
        return "common/_execute_script";
    }

    @DeleteMapping("/category")
    public String categoryDelete(@RequestParam("chk") List<Integer> chks, Model model) {
        commonProcess("category", model);

        categoryDeleteService.deleteList(chks);

        // 삭제 완료 후 -> 목록 새로고침
        model.addAttribute("script", "parent.location.reload()");
        return "common/_execute_script";
    }

    /**
     * 공통 처리 부분
     * @param mode
     * @param model
     */
    private void commonProcess(String mode, Model model) {
        mode = Objects.requireNonNullElse(mode, "list");
        String pageTitle = "상품 목록";

        List<String> addCommonScript = new ArrayList<>();
        List<String> addScript = new ArrayList<>();

        if (mode.equals("add") || mode.equals("edit")) {
            pageTitle = mode.equals("edit") ? "상품 수정" : "상품 등록";
            addCommonScript.add("ckeditor5/ckeditor");
            addCommonScript.add("fileManager");
            addScript.add("product/form");

        } else if (mode.equals("category")) {
            pageTitle = "상품 분류";
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("addScript", addScript);
        model.addAttribute("addCommonScript", addCommonScript);
        model.addAttribute("subMenuCode", mode);
    }
}