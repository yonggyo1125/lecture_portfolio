# 상품 


## 상품 분류 

### 메세지 코드 

> resources/messages/validations.properties

```properties
... 

# 관리자 - 상품 - 카테고리
NotBlank.requestCategory.cateCd=분류코드를 입력하세요.
NotBlank.requestCategory.cateNm=분류명을 입력하세요.
Duplicated.requestCategory.cateCd=이미 등록된 분류코드 입니다.
```

> resources/messages/errors.properties

```properties

...
NotFound.product.category=등록된 분류가 아닙니다.

```

## 관리자 메뉴 추가 

> admin/menus/Menu.java 

```java 
...
public class Menu {
    ...

    static {
        menus = new HashMap<>();
        
        ...
        
        // 상품관리
        menus.put("product", Arrays.asList(
                new MenuDetail("list", "상품목록", "/admin/product"),
                new MenuDetail("add", "상품등록", "/admin/product/add"),
                new MenuDetail("category", "상품분류", "/admin/product/category")
        ));
        
        ...
    }
    
    ...
}
```

> resources/templates/admin/outlines/_side.html

```html
...
<aside th:fragment="menus">
    ...

    <a th:href="@{/admin/product}" th:classappend="${menuCode} == 'product' ? 'on'">
        <i class="xi-list"></i> 상품 관리
    </a>
</aside>
```

### 엔티티 및 레포지토리 구성 

> product/entities/Category.java

```java
package org.choongang.product.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.commons.entities.BaseMember;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = @Index(name="idx_category_listOrder", columnList = "listOrder DESC, createdAt DESC"))
public class Category extends BaseMember {
    @Id
    @Column(length=30)
    private String cateCd; // 분류코드

    @Column(length=60, nullable = false)
    private String cateNm; // 분류명

    private int listOrder; // 진열 가중치

    private boolean active; // 사용 여부
}
```

> product/repositories/CategoryRepository.java

```java
package org.choongang.product.repositories;

import org.choongang.product.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface CategoryRepository extends JpaRepository<Category, String>, QuerydslPredicateExecutor<Category> {
    
}
```



### 서비스 구성

> product/service/CategorySaveService.java

```java
package org.choongang.product.service;

import lombok.RequiredArgsConstructor;
import org.choongang.admin.product.controllers.RequestCategory;
import org.choongang.commons.Utils;
import org.choongang.product.entities.Category;
import org.choongang.product.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorySaveService {

    private final CategoryRepository repository;
    private final Utils utils;

    public void save(RequestCategory form) {
        Category category = new ModelMapper().map(form, Category.class);

        repository.saveAndFlush(category);
    }

    /**
     * 목록 조회
     *
     * @param chks : checkbox 선택 번호
     */
    public void saveList(List<Integer> chks) {
        for (int chk : chks) {
            String cateCd = utils.getParam("cateCd_" + chk);
            Category category = repository.findById(cateCd).orElse(null);
            if (category == null) continue;

            category.setCateNm(utils.getParam("cateNm_" + chk));
            category.setActive(Boolean.parseBoolean(utils.getParam("active_" + chk)));
            category.setListOrder(Integer.parseInt(utils.getParam("listOrder_" + chk)));
        }

        repository.flush();
    }

}
```

> product/service/CategoryNotFoundException.java

```java
package org.choongang.product.service;

import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertBackException;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends AlertBackException {
    public CategoryNotFoundException() {
        super(Utils.getMessage("NotFound.product.category", "errors"), HttpStatus.NOT_FOUND);
    }
}
```

> product/service/CategoryInfoService.java

```java 
package org.choongang.product.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.choongang.commons.exceptions.UnAuthorizedException;
import org.choongang.member.MemberUtil;
import org.choongang.product.entities.Category;
import org.choongang.product.entities.QCategory;
import org.choongang.product.repositories.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
public class CategoryInfoService {

    private final CategoryRepository categoryRepository;
    private final MemberUtil memberUtil;

    /**
     * 분류 개별 조회
     *
     * @param cateCd
     * @return
     */
    public Category get(String cateCd) {
        Category category = categoryRepository.findById(cateCd)
                .orElseThrow(CategoryNotFoundException::new);

        // 관리자가 X, 미사용 중인 경우는 접근 불가
        if (!memberUtil.isAdmin() && !category.isActive()) {
            throw new UnAuthorizedException();
        }

        return category;
    }

    /**
     * 분류 목록
     *
     * @param isAll : true - 미사용, 사용 전부 목록으로 조회(관리자)
     *              : false - 사용중인 목록만 조회(프론트)
     * @return
     */
    public List<Category> getList(boolean isAll) {
        QCategory category = QCategory.category;
        BooleanBuilder builder = new BooleanBuilder();

        if (!isAll) { // 사용중인 분류만 조회
            builder.and(category.active.eq(true));
        }

        List<Category> items = (List<Category>) categoryRepository.findAll(builder, Sort.by(desc("listOrder"), asc("createdAt")));

        return items;
    }

    public List<Category> getList() {
        return getList(false); // 사용중 목록(프론트)
    }
}

```
> product/service/CategoryDeleteService.java

```java
package org.choongang.product.service;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.choongang.product.entities.Category;
import org.choongang.product.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryDeleteService {

    private final Utils utils;
    private final CategoryRepository categoryRepository;

    /**
     * 분류 개별 삭제
     *
     * @param cateCd : 분류 코드 
     */
    public void delete(String cateCd) {
        Category category = categoryRepository.findById(cateCd).orElseThrow(CategoryNotFoundException::new);
        categoryRepository.delete(category);

        categoryRepository.flush();
    }

    /**
     * 분류 목록 삭제
     *
     * @param chks : 목록 선택 순번
     */
    public void deleteList(List<Integer> chks) {
        chks.forEach(chk -> delete(utils.getParam("cateCd_" + chk)));
    }
}
```

### 관리자 컨트롤러 

> admin/product/controllers/RequestCategory.java

```java
package org.choongang.admin.product.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestCategory {

    @NotBlank
    private String cateCd; // 분류 코드

    @NotBlank
    private String cateNm; // 분류명

    private int listOrder; // 진열 가중치 - 내림차순 정렬 우선순위, 숫자가 클수록 먼저 노출
    private boolean active; // 사용 여부
}
```

> admin/product/controllers/CategoryValidator.java

```java
package org.choongang.admin.product.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.product.repositories.CategoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class CategoryValidator implements Validator {

    private final CategoryRepository repository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestCategory.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        /* cateCd : 분류 코드 중복 여부 체크 */
        RequestCategory form = (RequestCategory)target;
        String cateCd = form.getCateCd();

        if (StringUtils.hasText(cateCd) && repository.existsById(cateCd)) { // 이미 등록된 분류 코드이면
            errors.rejectValue("cateCd", "Duplicated");
        }
    }
}
```

> admin/product/controllers/ProductController.java

```java
package org.choongang.admin.product.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.menus.Menu;
import org.choongang.admin.menus.MenuDetail;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.choongang.product.entities.Category;
import org.choongang.product.service.CategoryDeleteService;
import org.choongang.product.service.CategoryInfoService;
import org.choongang.product.service.CategorySaveService;
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

    @ModelAttribute("menuCode")
    public String getMenuCode() {
        return "product";
    }

    @ModelAttribute("subMenus")
    public List<MenuDetail> getSubMenus() {
        return Menu.getMenus("product");
    }

    /**
     * 상품 목록
     *
     * @return
     */
    @GetMapping
    public String list(Model model) {
        commonProcess("list", model);

        return "admin/product/list";
    }

    /**
     * 상품 등록
     *
     * @param model
     * @return
     */
    @GetMapping("/add")
    public String add(Model model) {
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
    public String save(Model model) {

        return "redirect:/admin/product";
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
```

> resources/templates/product/list.html : 상품 목록 - 기초 구성 / 추후 상품 등록, 수정 구현시 코드 추가 예정

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">
<section layout:fragment="content">

</section>
</html>
```

> resources/templates/product/_form.html : 상품 등록,수정 공통 양식 - 기초 구성 / 추후 상품 등록, 수정 구현시 코드 추가 예정

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="form">

</th:block>
</html>
```

> resources/templates/product/add.html : 상품 등록 - 기초 구성 / 추후 상품 등록, 수정 구현시 코드 추가 예정

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">
<section layout:fragment="content">
    <h1>상품 등록</h1>
    <form name="frmSave" method="post" th:action="@{/admin/product/save}" autocomplete="off">
        <input type="hidden" name="mode" value="add">
        <th:block th:replace="~{admin/product/_form::form}"></th:block>

        <div class="submit_btns">
            <button type="reset" class="btn">다시입력</button>
            <button type="submit" class="btn">등록하기</button>
        </div>
    </form>
</section>
</html>
```

> resources/templates/product/edit.html : 상품 수정 - 기초 구성 / 추후 상품 등록, 수정 구현시 코드 추가 예정

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">
<section layout:fragment="content">
    <h1>상품 수정</h1>
    <form name="frmSave" method="post" th:action="@{/admin/product/save}" autocomplete="off">
        <input type="hidden" name="mode" value="edit">
        <th:block th:replace="~{admin/product/_form::form}"></th:block>

        <div class="submit_btns">
            <button type="reset" class="btn">다시입력</button>
            <button type="submit" class="btn">수정하기</button>
        </div>
    </form>
</section>
</html>
```

> resources/templates/product/category.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">
<section layout:fragment="content">
    <h1>분류 관리</h1>

    <h2>분류 등록</h2>
    <form name="frmPs" method="post" th:action="@{/admin/product/category}" target="ifrmProcess" autocomplete="off" th:object="${requestCategory}">
        <table class="table_cols">
            <tr>
                <th width="180">분류코드</th>
                <td width="300">
                    <input type="text" name="cateCd" th:field="*{cateCd}">
                </td>
                <th width="180">분류명</th>
                <td>
                    <input type="text" name="cateNm" th:field="*{cateNm}">
                </td>
            </tr>
            <tr>
                <th>진열가중치</th>
                <td>
                    <input type="number" name="listOrder" th:field="*{listOrder}">
                </td>
                <th>사용여부</th>
                <td>
                    <input type="radio" name="active" value="true" id="active_true" th:field="*{active}">
                    <label for="active_true">사용</label>

                    <input type="radio" name="active" value="false" id="active_false" th:field="*{active}">
                    <label for="active_false">미사용</label>
                </td>
            </tr>
        </table>
        <div class="submit_btns">
            <button type="reset" class="btn">다시입력</button>
            <button type="submit" class="btn">등록하기</button>
        </div>
    </form>

    <h2>분류 목록</h2>

    <form name="frmList" method="post" th:action="@{/admin/product/category}" target="ifrmProcess" autocomplete="off">
        <input type="hidden" name="_method" value="PATCH">
        <table class="table_rows">
            <thead>
            <tr>
                <th width="40">
                    <input type="checkbox" class="checkall" data-target-name="chk" id="checkall">
                    <label for="checkall"></label>
                </th>
                <th width="180">분류코드</th>
                <th width="200">분류명</th>
                <th width="150">진열가중치</th>
                <th width="180">사용여부</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <tr th:if="${items != null && !items.isEmpty()}" th:each="item, status : ${items}" th:object="${item}">
                <td align="center">
                    <input type="hidden" th:name="${'cateCd_' + status.index}" th:value="*{cateCd}">
                    <input type="checkbox" name="chk" th:value="${status.index}" th:id="${'chk_' + status.index}">
                    <label th:for="${'chk_' + status.index}"></label>
                </td>
                <td th:text="*{cateCd}" align="center"></td>
                <td>
                    <input type="text" th:name="${'cateNm_' + status.index}" th:value="*{cateNm}">
                </td>
                <td>
                    <input type="number" th:name="${'listOrder_' + status.index}" th:value="*{listOrder}">
                </td>
                <td align="center">
                    <input type="radio" th:name="${'active_' + status.index}" th:checked="*{active}" th:id="${'active_true_' + status.index}" value="true">
                    <label th:for="${'active_true_' + status.index}">사용</label>

                    <input type="radio" th:name="${'active_' + status.index}" th:checked="*{!active}" th:id="${'active_false_' + status.index}" value="false">
                    <label th:for="${'active_false_' + status.index}">미사용</label>
                </td>
                <td>
                    <a th:href="@{/admin/product?cateCd={cateCd}(cateCd=*{cateCd})}" class="sbtn">상품관리</a>
                    <a th:href="@{/product/{cateCd}(cateCd=*{cateCd})}" class="sbtn on" target="_blank">미리보기</a>
                </td>
            </tr>
            <tr th:unless="${items != null && !items.isEmpty()}">
                <td colspan="6" class="no_data">등록된 분류가 없습니다.</td>
            </tr>
            </tbody>
        </table>
        <div class="table_actions">
            <button type="button" class="sbtn form_action" data-mode="delete" data-form-name="frmList">선택 분류 삭제</button>

            <button type="button" class="sbtn on form_action" data-mode="edit" data-form-name="frmList">선택 분류 수정</button>
        </div>
    </form>

</section>
</html>

```

> commons/Utils.java

```java
...

public class Utils {
    ...

    /**
     * 요청 데이터 가져오기 편의 함수
     *
     * @param name
     * @return
     */
    public String getParam(String name) {
        return request.getParameter(name);
    }

    public String[] getParams(String name) {
        return request.getParameterValues(name);
    }
}
```



## 상품 등록 

## 상품 수정