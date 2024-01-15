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

> resources/templates/commons/_editor_file.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<span th:fragment="item" th:if="${item != null}" th:object="${item}" class="file_tpl_box" th:id="*{'file_' + seq}">
        <a th:href="@{/file/download/{seq}(seq=*{seq})}" th:text="*{fileName}"></a>
        <i class="xi-upload insert_image" th:data-url="*{fileUrl}"></i>
        <a th:href="@{/file/delete/{seq}(seq=*{seq})}" onclick="return confirm('정말 삭제하시겠습니까?');" class="remove" target="ifrmProcess">
            <i class="xi-close"></i>
        </a>
    </span>
</html>
```

> resources/templates/common/_image_file.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<div th:fragment="item" th:if="${item != null}" th:object="${item}" class="image1_tpl_box" th:id="*{'file_' + seq}">
    <a th:href="@{/file/delete/{seq}(seq=*{seq})}" onclick="return confirm('정말 삭제하시겠습니까?');" class="remove" target="ifrmProcess">
        <i class="xi-close"></i>
    </a>
    <div class="inner" th:style="${@utils.backgroundStyle(item)}"></div>
</div>
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

    public String backgroundStyle(FileInfo file) {

        String imageUrl = file.getFileUrl();
        List<String> thumbsUrl = file.getThumbsUrl();
        if (thumbsUrl != null && !thumbsUrl.isEmpty()) {
            imageUrl = thumbsUrl.get(thumbsUrl.size() - 1);
        }

        String style = String.format("background:url('%s') no-repeat center center; background-size:cover;", imageUrl);

        return style;
    }
}
```



## 상품 등록 

> resources/messages/commons.properties

```properties
...

# 상품 상태
ProductStatus.SALE=판매중
ProductStatus.OUT_OF_STOCK=품절
ProductStatus.PREPARE=상품준비중
```

> resources/messages/validations.properties

```properties

...

#관리자 - 상품 - 등록/수정
NotBlank.requestProduct.cateCd=분류코드를 선택하세요.
NotBlank.requestProduct.name=상품명을 입력하세요.
```

> resources/messages/errors.properties

```properties 
...

NotFound.product=등록된 상품이 아닙니다.
```

> product/constants/DiscountType.java : 할인 방식 - 퍼센트, 고정금액 

```java
package org.choongang.product.constants;

/**
 * 할인 종류
 *
 */
public enum DiscountType {
    PERCENT, // 상품가 퍼센트 비율 할인
    PRICE, // 고정 금액 할인
}
```

> product/constants/ProductStatus.java : 상품 상태
>
> 상품은 판매중, 상품 준비중 상태만 노출, 주문 및 장바구니 담기는 판매중 만 가능
> 
> 품절 상태는 2가지로 결정된다. 1) 상품 상태가 품절, 2) 재고 사용 중이고 재고가 0인 경우
> 
>상품이 품절 상태이라면 재고사용 중이고 재고가 남아 있어도 품절로 처리 

```java
package org.choongang.product.constants;

import org.choongang.commons.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * 상품 상태
 *
 */
public enum ProductStatus {
    SALE(Utils.getMessage("ProductStatus.SALE", "commons")), // 판매중
    OUT_OF_STOCK(Utils.getMessage("ProductStatus.OUT_OF_STOCK", "commons")), // 품절
    PREPARE(Utils.getMessage("ProductStatus.PREPARE", "commons")); // 상품 준비중

    private final String title;

    ProductStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * 상품 상태 목록 : 0 - 상수 문자열, 1 - 한글 문자열
     * @return
     */
    public static List<String[]> getList() {
        return Arrays.asList(
                new String[] {SALE.name(), SALE.title},
                new String[] {OUT_OF_STOCK.name(), OUT_OF_STOCK.title},
                new String[] {PREPARE.name(), PREPARE.title}
        );
    }
}
```

### 엔티티 및 레포지토리 구성

> product/entities/Product.java : 상품 엔티티

```java
package org.choongang.product.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.commons.entities.BaseMember;
import org.choongang.file.entities.FileInfo;
import org.choongang.product.constants.DiscountType;
import org.choongang.product.constants.ProductStatus;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseMember {

    @Id @GeneratedValue
    private Long seq; // 상품 번호

    @Column(length=65)
    private String gid = UUID.randomUUID().toString(); // 그룹 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cateCd")
    private Category category; // 상품 분류

    @Column(length=100, nullable = false)
    private String name; // 상품명

    private int consumerPrice; // 소비자가(보이는 금액)
    private int salePrice; // 판매가(결제 기준 금액)

    private boolean useStock; // 재고 사용 여부 - true : 재고 차감
    private int stock; // 옵션을 사용하지 않는 경우 단일 상품 재고, 0 - 무제한

    @Enumerated(EnumType.STRING)
    @Column(length=10, nullable = false)
    private DiscountType discountType = DiscountType.PERCENT;
    private int discount; // 할인 금액

    @Lob
    private String extraInfo; // 상품 추가 정보 : JSON 문자열로 저장

    private boolean packageDelivery; // 같은 판매자별 묶음 배송 여부
    private int deliveryPrice; // 배송비, 0이면 무료 배송

    @Lob
    private String description; // 상품 상세 설명

    private float score; // 평점

    private boolean active; // 노출 여부 : true -> 소비자 페이지 노출

    @Enumerated(EnumType.STRING)
    @Column(length=15, nullable = false)
    private ProductStatus status = ProductStatus.PREPARE; // 상품 상태

    private boolean useOption; // 옵션 사용 여부, true : 옵션 사용, 재고는 옵션쪽 재고 사용


    @Column(length=60)
    private String optionName; // 옵션명

    @Transient
    private List<FileInfo> mainImages; // 메인 이미지

    @Transient
    private List<FileInfo> listImages; // 목록 이미지

    @Transient
    private List<FileInfo> editorImages; // 에디터에 첨부한 이미지
    
    private int listOrder; // 진열 가중치
}
```

> product/entities/ProductOption.java : 상품 옵션 엔티티 
>
> 1개의 상품은 여러개의 옵션을 가질 수 있으므로 @ManyToOne으로 Product 엔티티와 관계 매핑

```java
package org.choongang.product.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.commons.entities.BaseMember;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes =
@Index(name="idx_pdt_opt_order", columnList = "listOrder DESC, createdAt ASC"))
public class ProductOption extends BaseMember {

    @Id @GeneratedValue
    private Long seq;

    @Column(length=80, nullable = false)
    private String name; // 옵션명

    private int addPrice; // 옵션 추가금액(-, +)

    private boolean useStock; // false : 무제한, true : 재고 0 -> 품절
    private int stock; // 옵션별 재고

    private int listOrder; // 진열 가중치, 번호가 클 수록 앞에 진열

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productSeq")
    private Product product;
}
```

> product/repositories/ProductRepository.java

```java
package org.choongang.product.repositories;

import org.choongang.product.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, QuerydslPredicateExecutor<Product> {

}
```

> product/repositories/ProductOptionRepository.java

```java
package org.choongang.product.repositories;

import org.choongang.product.entities.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long>, QuerydslPredicateExecutor<ProductOption> {

}
```

## 관리자 페이지 - 상품 등록 양식 구성 

> admin/product/controllers/RequestProduct.java

```java
package org.choongang.admin.product.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.choongang.file.entities.FileInfo;
import org.choongang.product.constants.DiscountType;
import org.choongang.product.constants.ProductStatus;

import java.util.List;
import java.util.UUID;

@Data
public class RequestProduct {

    private String mode = "add";

    private Long seq; // 상품 번호

    private String gid = UUID.randomUUID().toString(); // 그룹 ID

    @NotBlank
    private String cateCd; // 분류 코드

    @NotBlank
    private String name; // 상품명

    private int consumerPrice; // 소비자가(보이는 금액)
    private int salePrice; // 판매가(결제 기준 금액)

    private boolean useStock; // 재고 사용 여부 - true : 재고 차감
    private int stock; // 옵션을 사용하지 않는 경우 단일 상품 재고, 0 - 무제한

    private String discountType = DiscountType.PERCENT.name(); // 할인 종류 - PERCENT, PRICE
    private int discount; // 할인 금액

    private String extraInfo; // 상품 추가 정보 : JSON 문자열로 저장 - 양식에서는 hidden 값으로 업데이트 처리

    private boolean packageDelivery; // 같은 판매자별 묶음 배송 여부
    private int deliveryPrice; // 배송비, 0이면 무료 배송

    private String description; // 상품 상세 설명

    private boolean active; // 노출 여부 : true -> 소비자 페이지 노출

    private String status = ProductStatus.PREPARE.name(); // 상품 상태 - 상품 준비중이 기본 상태

    private boolean useOption; // 옵션 사용 여부, true : 옵션 사용, 재고는 옵션쪽 재고 사용

    private String optionName; // 옵션명

    private List<FileInfo> mainImages; // 메인 이미지

    private List<FileInfo> listImages; // 목록 이미지

    private List<FileInfo> editorImages; // 에디터에 첨부한 이미지
}
```

> admin/product/controllers/ProductController.java

```java

... 

public class ProductController implements ExceptionProcessor {
    ...
    
    private final FileInfoService fileInfoService;
    private final ProductSaveService productSaveService;
    private final ProductInfoService productInfoService;
    
    ...
    
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
    
    ...

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
        model.addAttribute("requestProduct", form);

        return "admin/product/edit";
    }
    
    ...
    
}
```

> resources/template/product/_form.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="form" th:object="${requestProduct}">
    <input type="hidden" name="gid" th:field="*{gid}">
    <h2>기본 정보</h2>
    <table class="table_cols mb30">
        <tr th:if="*{mode == 'edit' && seq != null}">
            <th width="180">상품번호</th>
            <td>
                <input type="hidden" name="seq" th:field="*{seq}">
                <th:block th:text="*{seq}"></th:block>
            </td>
        </tr>
        <tr>
            <th>노출여부</th>
            <td>
                <input type="radio" name="active" value="true" id="active_true" th:field="*{active}">
                <label for="active_true">노출</label>
                <input type="radio" name="active" value="false" id="active_false" th:field="*{active}">
                <label for="active_false">미노출</label>
            </td>
        </tr>
        <tr>
            <th>상품상태</th>
            <td>
                <th:block th:each="s, sts : ${productStatuses}">
                    <input type="radio" name="status" th:value="${s[0]}" th:id="${'status_' + sts.index}" th:field="*{status}">
                    <label th:for="${'status_' + sts.index}" th:text="${s[1]}"></label>
                </th:block>
            </td>
        </tr>
        <tr>
            <th width="180">분류코드</th>
            <td>
                <select name="cateCd" th:field="*{cateCd}">
                    <option value="">- 선택하세요 -</option>
                    <option th:each="category : ${categories}" th:value="${category.cateCd}" th:text="${#strings.concat(category.cateNm,'(', category.cateCd, ')')}"></option>
                </select>
                <div class="error" th:each="err : ${#fields.errors('cateCd')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>상품명</th>
            <td>
                <input type="text" name="name" th:field="*{name}">
                <div class="error" th:each="err : ${#fields.errors('name')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>소비자가</th>
            <td>
                <input type="number" name="consumerPrice" th:field="*{consumerPrice}" class="w200">
                <div class="error" th:each="err : ${#fields.errors('consumerPrice')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>판매가</th>
            <td>
                <input type="text" name="salePrice" th:field="*{salePrice}" class="w200">
                <div class="error" th:each="err : ${#fields.errors('salePrice')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>상품할인</th>
            <td>
                <div class="input_grp">
                    <input type="number" name="discount" th:field="*{discount}" class="w200 ar">
                    <select name="discountType" th:field="*{discountType}">
                        <option value="PERCENT">%</option>
                        <option value="PRICE">원</option>
                    </select>
                </div>
                <div class="error" th:each="err : ${#fields.errors('discount')}" th:text="${err}"></div>
                <div class="error" th:each="err : ${#fields.errors('discountType')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>묶음배송</th>
            <td>
                <input type="radio" name="packageDelivery" value="true" th:field="*{packageDelivery}" id="packageDelivery_true">
                <label for="packageDelivery_true">사용</label>
                <input type="radio" name="packageDelivery" value="false" th:field="*{packageDelivery}" id="packageDelivery_false">
                <label for="packageDelivery_false">미사용</label>
                <div class="error" th:each="err : ${#fields.errors('packageDelivery')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>배송비</th>
            <td>
                <input type="number" name="deliveryPrice" th:field="*{deliveryPrice}" class="w200">원
                <div class="error" th:each="err : ${#fields.errors('deliveryPrice')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>재고</th>
            <td>
                <div class="input_grp">
                    <select name="useStock" th:field="*{useStock}">
                        <option value="true">사용</option>
                        <option value="false">미사용</option>
                    </select>
                    <input type="number" name="stock" th:field="*{stock}" class="w200 ar">개
                </div>
                <div class="error" th:each="err : ${#fields.errors('stock')}" th:text="${err}"></div>
            </td>
        </tr>
    </table>

    <h2>상품 이미지</h2>
    <table class="table_cols mb30">
        <tr>
            <th width="180">메인이미지</th>
            <td>
                <div class="uploaded_files" id="main_files">
                    <th:block th:each="item : *{mainImages}">
                        <span th:replace="~{common/_image_file::item}"></span>
                    </th:block>
                </div>
                <button type="button" class="sbtn upload_files" data-location="main" data-image-only="true">
                    <i class="xi-image"></i>
                    이미지 추가
                </button>
            </td>
        </tr>
        <tr>
            <th width="180">목록이미지</th>
            <td>
                <div class="uploaded_files" id="list_files">
                    <th:block th:each="item : *{listImages}">
                        <span th:replace="~{common/_image_file::item}"></span>
                    </th:block>
                </div>
                <button type="button" class="sbtn upload_files" data-location="list" data-image-only="true" data-single-file="true">
                    <i class="xi-image"></i>
                    이미지 추가
                </button>
            </td>
        </tr>
    </table>

    <h2>추가정보</h2>
    <table class="table_cols mb30">
        <tr>
            <th width="180">내용(JSON)</th>
            <td>
                <textarea name="extraInfo" th:field="*{extraInfo}"></textarea>
                <div class="error" th:each="err : ${#fields.errors('extraInfo')}" th:text="${err}"></div>
            </td>
        </tr>
    </table>

    <h2>옵션 정보</h2>
    <table class="table_cols mb30">
        <tr>
            <th width="180">사용여부</th>
            <td>
                <input type="radio" name="useOption" value="true" th:field="*{useOption}" id="useOption_true">
                <label for="useOption_true">사용</label>
                <input type="radio" name="useOption" value="false" th:field="*{useOption}" id="useOption_false">
                <label for="useOption_false">미사용</label>
                <div class="error" th:each="err : ${#fields.errors('useOption')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>옵션명</th>
            <td>
                <input type="text" name="optionName" th:field="*{optionName}">
                <div class="error" th:each="err : ${#fields.errors('optionName')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>옵션목록</th>
            <td></td>
        </tr>
    </table>
    <h2>상세 설명</h2>
    <div class="mb10">
        <textarea name="description" th:field="*{description}" id="description"></textarea>
    </div>
    <button type="button" class="sbtn upload_files" data-location="editor" data-image-only="true" data-single-file="true">
        <i class="xi-image"></i>
        이미지 추가
    </button>
    <div class="uploaded_files" id="editor_files">
        <th:block th:each="item : *{editorImages}">
            <span th:replace="~{common/_editor_file::item}"></span>
        </th:block>
    </div>
    <script th:replace="~{common/_file_tpl::image1_tpl}"></script>
    <script th:replace="~{common/_file_tpl::editor_tpl}"></script>
</th:block>
</html>
```

> resources/static/admin/js/product/form.js

```javascript
window.addEventListener("DOMContentLoaded", function() {
    /* 상세 설명 에디터 로드 S */
    const { loadEditor } = commonLib;

    loadEditor("description", 450)
        .then(editor => window.editor = editor);

    /* 상세 설명 에디터 로드 E */

    /* 이미지 본문 추가 이벤트 처리 S */
    const insertImages = document.getElementsByClassName("insert_image");
    for (const el of insertImages) {
        el.addEventListener("click", function() {
            const parentId = this.parentElement.parentElement.id;
            const url = this.dataset.url;

            insertImage(url);
        });
    }
    /* 이미지 본문 추가 이벤트 처리 E */

});

/**
* 파일 업로드 후속 처리
*
*/
function callbackFileUpload(files) {
    const editorTpl = document.getElementById("editor_tpl").innerHTML;
    const imageTpl = document.getElementById("image1_tpl").innerHTML;

    const domParser = new DOMParser();
    const mainImageEl = document.getElementById("main_files");
    const listImageEl = document.getElementById("list_files");
    const editorImageEl = document.getElementById("editor_files");

    for (const file of files) {
        const location = file.location;
        let targetEl, html;
        switch (location) {
            case "main":
                html = imageTpl;
                targetEl = mainImageEl;
                break;
            case "list":
                html = imageTpl;
                targetEl = listImageEl;
                break;
            default :
                html = editorTpl;
                targetEl = editorImageEl;
                insertImage(editor, file.fileUrl); // 에디터에 이미지 추가
        }

         /* 템플릿 데이터 치환 S */
         html = html.replace(/\[seq\]/g, file.seq)
                    .replace(/\[fileName\]/g, file.fileName)
                    .replace(/\[imageUrl\]/g, file.fileUrl);

         const dom = domParser.parseFromString(html, "text/html");
         const fileBox = location == 'editor' ? dom.querySelector(".file_tpl_box") :  dom.querySelector(".image1_tpl_box")
         console.log(fileBox);
         targetEl.appendChild(fileBox);


         const el = fileBox.querySelector(".insert_image")
         if (el) {
            // 이미지 본문 추가 이벤트
            el.addEventListener("click", () => insertImage(file.fileUrl));
         }
         /* 템플릿 데이터 치환 E */
    }
}


/**
* 에디터에 이미지 추가
*
*/
function insertImage(source) {
    editor.execute('insertImage', { source });
}

/**
* 파일 삭제 후 후속 처리
*
* @param seq : 파일 등록 번호
*/
function callbackFileDelete(seq) {
    const fileBox = document.getElementById(`file_${seq}`);
    fileBox.parentElement.removeChild(fileBox);
}
```

### 관리자 - 상품 목록

> admin/product/controllers/ProductController.java

```java
...

public class ProductController implements ExceptionProcessor {
    
    ...
    
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
    
    ...
}
```

> resources/templates/admin/product/list.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">
<section layout:fragment="content">
    <h1>상품 관리</h1>
    <h2>상품 검색</h2>

    <h2>상품 목록</h2>
    <form name="frmList" method="post" th:action="@{/admin/product}" target="ifrmProcess" autocomplete="off">
        <table class="table_rows">
            <thead>
                <tr>
                    <th width="40">
                        <input type="checkbox" class="checkall" id="checkall" data-target-name="chk">
                        <label for="checkall"></label>
                    </th>
                    <th width="150">상품번호</th>
                    <th nowrap colspan="2">상품명</th>
                    <th width="150">노출여부</th>
                    <th width="150">진열가중치</th>
                    <th width="250"></th>
                </tr>
            </thead>
            <tbody>
                <tr th:if="${items == null || items.isEmpty()}">
                    <td colspan="7" class="no_data">조회된 상품이 없습니다.</td>
                </tr>
                <tr th:unless="${items == null || items.isEmpty()}" th:each="item, status : ${items}" th:object="${item}">
                    <td>
                        <input type="hidden" th:name="${'seq_' + status.index}" th:value="*{seq}">
                        <input type="checkbox" name="chk" th:value="${status.index}" th:id="${'chk_' + status.index}">
                        <label th:for="${'chk_' + status.index}"></label>
                    </td>
                    <td th:text="*{seq}"></td>
                    <td width="80">이미지</td>
                    <td th:text="*{name}"></td>
                    <td></td>
                    <td></td>
                    <td>
                        <a th:href="@{/admin/product/edit/{seq}(seq=*{seq})}" class="sbtn">수정하기</a>
                        <a th:href="@{/product/detail/{seq}(seq=*{seq})}" class="sbtn on" target="_blank">미리보기</a>
                    </td>
                </tr>
            </tbody>
        </table>
    </form>
    <th:block th:replace="~{common/_pagination::pagination}"></th:block>
</section>
</html>
```


### 서비스 구현 

> product/service/ProductNotFoundException.java

```java
package org.choongang.product.service;

import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertBackException;
import org.springframework.http.HttpStatus;

/**
 * 상품이 조회되지 않는 경우 발생하는 예외
 */
public class ProductNotFoundException extends AlertBackException {
    public ProductNotFoundException() {
        super(Utils.getMessage("NotFound.product", "errors"), HttpStatus.NOT_FOUND);
    }
}
```

> product/service/ProductSaveService.java 

```java
package org.choongang.product.service;

import lombok.RequiredArgsConstructor;
import org.choongang.admin.product.controllers.RequestProduct;
import org.choongang.file.service.FileUploadService;
import org.choongang.product.constants.DiscountType;
import org.choongang.product.constants.ProductStatus;
import org.choongang.product.entities.Category;
import org.choongang.product.entities.Product;
import org.choongang.product.repositories.CategoryRepository;
import org.choongang.product.repositories.ProductOptionRepository;
import org.choongang.product.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductSaveService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final FileUploadService fileUploadService;
    private final CategoryRepository categoryRepository;
    public void save(RequestProduct form) {

        String mode = form.getMode();
        Long seq = form.getSeq();

        Product product = null;

        if (mode.equals("edit") && seq != null) {
            product = productRepository.findById(seq).orElseThrow(ProductNotFoundException::new);

        } else {
            product = new Product();
            product.setGid(form.getGid());
        }

        /* 분류 설정 S */
        String cateCd = form.getCateCd();
        if (StringUtils.hasText(cateCd)) {
            Category category = categoryRepository.findById(cateCd).orElse(null);
            product.setCategory(category);
        }
        /* 분류 설정 E */

        product.setName(form.getName());
        product.setConsumerPrice(form.getConsumerPrice());
        product.setSalePrice(form.getSalePrice());

        product.setStatus(ProductStatus.valueOf(form.getStatus()));
        product.setDiscountType(DiscountType.valueOf(form.getDiscountType()));

        product.setUseStock(form.isUseStock());
        product.setStock(form.getStock());
        product.setExtraInfo(form.getExtraInfo());
        product.setPackageDelivery(form.isPackageDelivery());
        product.setDeliveryPrice(form.getDeliveryPrice());
        product.setDescription(form.getDescription());
        product.setActive(form.isActive());
        product.setUseOption(form.isUseOption());
        product.setOptionName(form.getOptionName());

        fileUploadService.processDone(product.getGid());
    }
}
```

> product/controllers/ProductSearch.java 

```java
package org.choongang.product.controllers;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProductSearch {
    private int page = 1;
    private int limit = 20;

    private List<String> cateCd; // 카테고리 검색
    private List<Long> seq; // 상품 번호
    private String name; // 상품 명

    private List<String> statuses; // 상품 상태

    private LocalDate sdate; // 날짜 검색 시작
    private LocalDate edate; // 날짜 검색 종료
}
```

> product/service/ProductInfoService.java

```java
package org.choongang.product.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.product.controllers.RequestProduct;
import org.choongang.commons.ListData;
import org.choongang.commons.Pagination;
import org.choongang.commons.Utils;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.service.FileInfoService;
import org.choongang.product.constants.ProductStatus;
import org.choongang.product.controllers.ProductSearch;
import org.choongang.product.entities.Category;
import org.choongang.product.entities.Product;
import org.choongang.product.entities.QProduct;
import org.choongang.product.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
public class ProductInfoService {

    private final ProductRepository productRepository;
    private final FileInfoService fileInfoService;
    private final HttpServletRequest request;

    /**
     * 상품 개별 조회
     *
     * @param seq : 상품 번호
     * @return
     */
    public Product get(Long seq) {
        Product product = productRepository.findById(seq).orElseThrow(ProductNotFoundException::new);

        addProductInfo(product);

        return product;

    }

    /**
     * Product -> RequestProduct 변환
     *
     * @param seq
     * @return
     */
    public RequestProduct getForm(Long seq) {
        Product product = get(seq);
        RequestProduct form = new ModelMapper().map(product, RequestProduct.class);

        Category category = product.getCategory();
        if (category != null) {
            form.setCateCd(category.getCateCd());
        }

        form.setStatus(product.getStatus().name());
        form.setDiscountType(product.getDiscountType().name());

        form.setMode("edit");

        return form;
    }

    /**
     * 상품 목록 조회
     *
     * @param search : 검색 조건
     * @param isAll : true - 미노출 상품도 모두 보이기 
     * @return
     */
    public ListData<Product> getList(ProductSearch search, boolean isAll) {
        int page = Utils.onlyPositiveNumber(search.getPage(), 1);
        int limit = Utils.onlyPositiveNumber(search.getLimit(), 20);

        QProduct product = QProduct.product;
        BooleanBuilder andBuilder = new BooleanBuilder();

        /* 검색 조건 처리 S */
        List<String> cateCd = search.getCateCd();
        List<Long> seq = search.getSeq();
        List<String> statuses = search.getStatuses();
        LocalDate sdate = search.getSdate();
        LocalDate edate = search.getEdate();
        String name = search.getName();

        // 상품 분류 처리
        if (cateCd != null && !cateCd.isEmpty()) {
            andBuilder.and(product.category.cateCd.in(cateCd));
        }

        // 상품 번호 처리
        if (seq != null && !seq.isEmpty()) {
            andBuilder.and(product.seq.in(seq));
        }

        // 상품 상태 처리
        if (statuses != null && !statuses.isEmpty()) {
            List<ProductStatus> _statuses = statuses.stream().map(ProductStatus::valueOf).toList();
            andBuilder.and(product.status.in(_statuses));
        }

        // 상품 등록일자 검색 처리 S
        if (sdate != null) {
            andBuilder.and(product.createdAt.goe(LocalDateTime.of(sdate, LocalTime.of(0,0,0))));
        }

        if (edate != null) {
            andBuilder.and(product.createdAt.loe(LocalDateTime.of(edate, LocalTime.of(23, 59, 59))));
        }
        // 상품 등록일자 검색 처리 E

        // 상품명 검색
        if (StringUtils.hasText(name)) {
            andBuilder.and(product.name.contains(name.trim()));
        }

        // 키워드 검색 처리 E

        /* 검색 조건 처리 E */

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(desc("listOrder"), desc("createdAt")));

        Page<Product> data = productRepository.findAll(andBuilder, pageable);
        Pagination pagination = new Pagination(page, (int)data.getTotalElements(), 10, limit, request);

        List<Product> items = data.getContent();
        items.forEach(this::addProductInfo); // 추가 정보 처리

        return new ListData<>(items, pagination);
    }

    /**
     * 상품 추가 정보 처리
     *
     * @param product
     */
    public void addProductInfo(Product product) {
        String gid = product.getGid();

        List<FileInfo> editorImages = fileInfoService.getListDone(gid, "editor");
        List<FileInfo> mainImages = fileInfoService.getListDone(gid, "main");
        List<FileInfo> listImages = fileInfoService.getListDone(gid, "list");

        product.setEditorImages(editorImages);
        product.setMainImages(mainImages);
        product.setListImages(listImages);
    }
}
```

> product/service/ProductDeleteService.java

```java
package org.choongang.product.service;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.choongang.file.service.FileDeleteService;
import org.choongang.product.entities.Product;
import org.choongang.product.repositories.ProductOptionRepository;
import org.choongang.product.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDeleteService {
    private final ProductInfoService productInfoService;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final FileDeleteService fileDeleteService;
    private final Utils utils;

    public void delete(Long seq) {
        Product product = productInfoService.get(seq);

        String gid = product.getGid();
        fileDeleteService.delete(gid);

        productRepository.delete(product);
        productRepository.flush();
    }

    /**
     * 상품 목록에서 삭제
     * 
     * @param chks
     */
    public void deleteList(List<Integer> chks) {
        for (int chk : chks) {
            Long seq = Long.parseLong(utils.getParam("seq_" + chk));
            delete(seq);
        }
    }
}
```

> admin/product/controllers/ProductController.java

```java
...

public class ProductController implements ExceptionProcessor {
    ...

    private final ProductSaveService productSaveService;
    private final ProductInfoService productInfoService;
    
    ...

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
            return "admin/product/" + mode;
        }

        productSaveService.save(form);

        return "redirect:/admin/product";
    }
    
    ...
}
```
## 상품 수정



## 사용자 - 상품 목록

> 상품 분류로 목록 출력 

> product/controllers/ProductAdvice.java : 상품 공통 데이터

```java 
package org.choongang.product.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.product.entities.Category;
import org.choongang.product.service.CategoryInfoService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice("org.choongang")
@RequiredArgsConstructor
public class ProductAdvice {
    private final CategoryInfoService categoryInfoService;

    /**
     * 상품 분류는 사용자 페이지 전역에 유지 되므로 전역 속성으로 정의
     * @return
     */
    @ModelAttribute("categories")
    private List<Category> getCategories() {
        return categoryInfoService.getList();
    }

}
```

> product/controllers/ProductController.java

```java
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
```

> resources/templates/front/product/list.html


```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/main}">
<main layout:fragment="content">
    <h1>
        <a th:href="@{/product/{cateCd}(cateCd=${category.cateCd})}" th:text="${category.cateNm}"></a>
    </h1>

    <ul class="products">
        <li th:if="${items != null && !items.isEmpty()}" th:each="item : ${items}" th:object="${item}" class="item">
            <a class="image" th:href="@{/product/detail/{seq}(seq=*{seq})}">
                <th:block th:if="*{listImages != null && !listImages.isEmpty()}" th:utext="*{@utils.printThumb(listImages[0].seq, 350, 350, 'goodsImage')}">
                </th:block>
                <th:block th:unless="*{listImages != null && !listImages.isEmpty()}">
                    이미지 없음(이미지로 교체)
                </th:block>
            </a>
            <div class="productNm" th:text="*{name}"></div>
            <del class="consumerPrice" th:if="*{consumerPrice > 0}">
                <th:block th:text="*{consumerPrice > 1000 ? #numbers.formatInteger(consumerPrice, 3, 'COMMA') : consumerPrice}"></th:block>
                <th:block th:text="#{원}"></th:block>
            </del>
            <div class="salePrice" th:if="*{salePrice > 0}">
                <th:block th:text="*{salePrice > 1000 ? #numbers.formatInteger(salePrice, 3, 'COMMA') : salePrice}"></th:block>
                <th:block th:text="#{원}"></th:block>
            </div>
        </li>
        <li th:unless="${items != null && !items.isEmpty()}" th:text="#{조회된_상품이_없습니다.}"></li>
    </ul>
    <th:block th:replace="~{common/_pagination::pagination}"></th:block>
</main>
</html>
```

> resources/templates/front/product/detail.html


```html

```

> resources/templates/front/product/_categories.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="menus">
<nav th:if="${categories != null && !categories.isEmpty()}">
    <a th:each="item : ${categories}" th:object="${item}" th:href="@{/product/{cateCd}(cateCd=*{cateCd})}" th:text="*{cateNm}"></a>
</nav>
</th:block>
</html>
```


> resources/templates/front/outlines/header.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
    xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
    <header th:fragment="common">
        ...
        
        <th:block th:replace="~{front/product/_categories::menus}"></th:block>
    </header>
</html>
```