# 레서피 게시판

## 컨트롤러 및 프론트 페이지 양식 구성

> resources/messages/commons.properties

```properties

...

# 레서피
레서피=레서피
레서피_작성=레서피 작성
레서피_수정=레서피 수정
대표_이미지_업로드=대표 이미지 업로드
레서피의_이름이_무엇인가요?=레서피의 이름이 무엇인가요?
레시피에_대한_간단한_설명을_덧붙여주세요.=레시피에 대한 간단한 설명을 덧붙여주세요.
예상_소요_시간=예상 소요 시간
15분_컷=15분 컷
30분_컷=30분 컷
45분_컷=45분 컷
46분_컷=46분 컷
기준량=기준량
인분=인분
필수재료=필수재료
부재료=부재료
양념=양념
완료=완료
카테고리=카테고리
요리_분류=요리 분류
요리_종류=요리 종류
```

> recipe/controllers/RequestRecipe.java

```java
package org.choongang.recipe.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class RequestRecipe {
    private String mode = "add";
    private Long seq;

    private String gid = UUID.randomUUID().toString();

    @NotBlank
    private String rcpName;
    private String rcpInfo;

    private int estimatedT;

    private String category;
    private String subCategory;
    private int amount;

    private String requiredIng;
    private String subIng;
    private String condiments;
}
```

> recipe/controllers/RecipeController.java

```java
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
```

> resources/templates/front/recipe/_form.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="form" th:object="${requestRecipe}">
    <div class="error global" th:each="err : ${#fields.globalErrors()}" th:text="${err}"></div>

    <input type="hidden" name="gid" th:field="*{gid}">
    <input type="hidden" name="requiredIngJSON" th:field="*{requiredIngJSON}">
    <input type="hidden" name="subIngJSON" th:field="*{subIngJSON}">
    <input type="hidden" name="condimentsJSON" th:field="*{condimentsJSON}">

    <div class="main_image_box">
        <div class="upload_files" id="main_images" data-image-only="true">
            <i class="xi-plus"></i>
            <div class="txt" th:text="#{대표_이미지_업로드}"></div>
        </div>
        <div class="thumbs"></div>
    </div>
    <!--// main_image_box -->

    <div class="rows recipe_name">
        <input type="text" name="rcpName" th:placeholder="#{레서피의_이름이_무엇인가요?}" th:field="*{rcpName}">
        <div class="error" th:each="err : ${#fields.errors('rcpName')}" th:text="${err}"></div>
    </div>
    <div class="rows">
        <textarea name="rcpInfo" th:placeholder="#{레시피에_대한_간단한_설명을_덧붙여주세요.}"></textarea>
    </div>

    <div class="stit" th:text="#{예상_소요_시간}"></div>
    <div class="rows estimate_times">
        <input type="radio" name="estimatedT" value="15" id="estimatedT_15" th:field="*{estimatedT}">
        <label for="estimatedT_15" th:text="#{15분_컷}"></label>

        <input type="radio" name="estimatedT" value="30" id="estimatedT_30" th:field="*{estimatedT}">
        <label for="estimatedT_30" th:text="#{30분_컷}"></label>

        <input type="radio" name="estimatedT" value="45" id="estimatedT_45" th:field="*{estimatedT}">
        <label for="estimatedT_45" th:text="#{45분_컷}"></label>

        <input type="radio" name="estimatedT" value="46" id="estimatedT_46" th:field="*{estimatedT}">
        <label for="estimatedT_46" th:text="#{46분_컷}"></label>
    </div>
    <div class="rows">
        <div class="stit" th:text="#{카테고리}"></div>
        <select name="category" th:field="*{category}">
            <option value="" th:text="#{요리_분류}"></option>
        </select>
        <select name="subCategory" th:field="*{subCategory}">
            <option value="" th:text="#{요리_종류}"></option>
        </select>
    </div>
    <div class="rows">
        <div class="stit" th:text="#{기준량}"></div>
        <div class="amount_box">
            <button type="button" class="down"><i class="xi-minus"></i></button>
            <input type="number" name="amount" min="1" th:field="*{amount}">
            <button type="button" class="up"><i class="xi-plus"></i></button>
            <th:block th:text="#{인분}"></th:block>
        </div>
    </div>
    <div class="rows">
        <div class="stit" th:text="#{필수재료}"></div>

        <div class="input_item_box" id="requiredIng"></div>
        <button type="button" th:text="#{추가}" data-id="requiredIng" data-name="requiredIng" class="add_input_item"></button>
    </div>
    <div class="rows">
        <div class="stit" th:text="#{부재료}"></div>

        <div class="input_item_box" id="subIng"></div>
        <button type="button" th:text="#{추가}" data-id="subIng" data-name="subIng" class="add_input_item"></button>
    </div>
    <div class="rows">
        <div class="stit" th:text="#{양념}"></div>

        <div class="input_item_box" id="condiments"></div>
        <button type="button" th:text="#{추가}" data-id="condiments" data-name="condiments" class="add_input_item"></button>
    </div>
    <th:block th:replace="~{common/_file_tpl::image1_tpl}"></th:block>
</th:block>
</html>
```

> resources/templates/front/recipe/add.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/main}">
<main layout:fragment="content" class="recipe_form">
    <form name="frmSave" method="post" th:action="@{/recipe/save}" autocomplete="off">
        <input type="hidden" name="mode" value="add">
        <th:block th:replace="~{front/recipe/_form::form}"></th:block>
        <button type="submit" th:text="#{완료}"></button>
    </form>
</main>
</html>
```

> resources/templates/front/recipe/edit.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/main}">
<main layout:fragment="content" class="recipe_form">
    <form name="frmSave" method="post" th:action="@{/recipe/save}" autocomplete="off">
        <input type="hidden" name="mode" value="edit">
        <th:block th:replace="~{front/recipe/_form::form}"></th:block>
        <button type="submit" th:text="#{완료}"></button>
    </form>
</main>
</html>
```

> resources/template/common/_file_tpl.html

```
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<script type="text/html" id="image1_tpl" th:fragment="image1_tpl">
    <div class="image1_tpl_box" id="file_[seq]" data-url="[imageUrl]">
        <a th:href="@{/file/delete/[seq]}" onclick="return confirm('정말 삭제하시겠습니까?');" class="remove" target="ifrmProcess">
            <i class="xi-close"></i>
        </a>
        <div class="inner" style="background:url('[imageUrl]') no-repeat center center; background-size: cover;"></div>
    </div>
</script>

...

</html>
```

> resources/static/css/recipe/style.css

```css
.recipe_form { width: 400px; margin: 0 auto; padding: 0 10px; }
/* 공통 */
.stit { font-weight: 500; margin: 15px 0 10px; }

/* 대표이미지 S */
#main_images { background: #d5d5d5; width: 400px; height: 400px; display: flex;  flex-direction: column; align-items: center; justify-content: center; color: #222; margin: 0 auto; }
#main_images i { font-size: 3rem; cursor: pointer; }
#main_images.uploaded i, #main_images.uploaded .txt { display: none; }
.image1_tpl_box { width: 62px; cursor: pointer; }
.image1_tpl_box > .inner { width: 60px; height: 60px; }
.main_image_box .thumbs { display: flex; }
.main_image_box .thumbs .image1_tpl_box { margin: 3px 0; }
.main_image_box .thumbs .image1_tpl_box + .image1_tpl_box { margin-left: 3px; }
/* 대표이미지 E */

input[type='text'],
input[type='password'],
input[type='number']{ width: 100%; height: 45px; border: 1px solid #d5d5d5; padding; 0 10px; }
textarea { border: 1px solid #d5d5d5; width: 100%; resize: none; height: 100px; padding: 10px; }

/* 예상 소요시간 S */
.estimate_times input[type='radio'] { display: none; }
.estimate_times input[type='radio']+label { display: inline-block; border: 2px solid #222; background: #fff; color: #222; font-size: 0.95rem; padding: 0 10px; height: 30px; line-height: 26px; cursor: pointer; }
.estimate_times input[type='radio']:checked+label { background: #222; color: #fff; }
/* 예상 소요시간 E */
```

> resources/static/js/recipe/form.js 

```javascript
const recipeForm = {
    init() {
        // 필수재료, 부재료, 양념 등 완성 처리
        const requiredIngJSON = document.getElementById("requiredIngJSON");

        const subIngJSON = document.getElementById("subIngJSON");

        const condimentsJSON = document.getElementById("condimentsJSON");

        if (requiredIngJSON) {

        }

    },
    /**
    * 입력항목 추가
    *
    */
    addItem(e) {
        const el = e.currentTarget;
        const inputBox = recipeForm.createItem(el.dataset.name);
        const targetEl = document.getElementById(el.dataset.id);
        if (targetEl) {
            targetEl.appendChild(inputBox);
        }
    },
    /**
    * 입력항목 제거
    *
    */
    deleteItem(e) {
        const el = e.currentTarget;
        const parentEl = el.parentElement;
        parentEl.parentElement.removeChild(parentEl);
    },
    /**
    * 입력항목 생성
    *
    */
    createItem(name) {
        const rows = document.createElement("div");
        const inputText = document.createElement("input");
        const inputEa = document.createElement("input");
        const closeButton = document.createElement("button");
        const buttonIcon = document.createElement("i");
        inputText.placeholder="예) 당근";
        inputEa.placeholder="1 개";

        rows.className = "item_box";

        inputText.type = "text";
        inputEa.type="text";

        inputText.name = name;
        inputEa.name = `${name}Ea`;

        closeButton.type = "button";
        buttonIcon.className = "remove_item xi-close";

        closeButton.appendChild(buttonIcon);

        rows.appendChild(inputText);
        rows.appendChild(inputEa);
        rows.appendChild(closeButton);

        closeButton.addEventListener("click", this.deleteItem);

        return rows;
    }
};

window.addEventListener("DOMContentLoaded", function() {

    recipeForm.init();

    const thumbs = document.getElementsByClassName("image1_tpl_box");
    for (const el of thumbs) {
        thumbsClickHandler(el);
    }

    /* 입력 항목 추가 버튼 처리 S */
    const buttons = document.getElementsByClassName("add_input_item");
    for (const el of buttons) {
        el.addEventListener("click", recipeForm.addItem);
    }
    /* 입력 항목 추가 버튼 처리 E */

    /* 입력 항목 제거 버튼 처리 S */
    const closeButtons = document.getElementsByClassName("remove_item");
    for (const el of closeButtons) {
        el.addEventListener("click", recipeForm.deleteItem)
    }
    /* 입력 항목 제거 버튼 처리 E */

    /* 기준량 버튼 이벤트 처리 S */
    const amountButtons = document.querySelectorAll(".amount_box button");
    const amountInputEa = document.querySelector(".amount_box input[type='number']");
    for (const el of amountButtons) {
        el.addEventListener("click", function() {
            let ea = amountInputEa.value;
            ea = isNaN(ea) ? 1 : Number(ea);
            if (this.classList.contains("down")) { // 수량 감소
                ea--
            } else { // 수량 증가
                ea++;
            }

            ea = ea < 1 ? 1 : ea;

            amountInputEa.value = ea;
        });
    }
    /* 기준량 버튼 이벤트 처리 E */
});

/**
* 파일 업로드 처리 콜백
*
*/
function callbackFileUpload(files) {
    const tpl = document.getElementById("image1_tpl").innerHTML;
    const domParser = new DOMParser();
    const targetEl = document.getElementById("main_images");
    const thumbsEl = document.querySelector(".main_image_box .thumbs");
    for (const file of files) {

        let html = tpl;
        html = html.replace(/\[seq\]/g, file.seq)
                    .replace(/\[imageUrl\]/g, file.fileUrl)
                    .replace(/\[fileName\]/g, file.fileName);

        const dom = domParser.parseFromString(html, "text/html");
        const imageBox = dom.querySelector(".image1_tpl_box");

        if (!targetEl.classList.contains("uploaded")) {
            targetEl.classList.add("uploaded")
        }

        targetEl.style.backgroundImage=`url('${file.fileUrl}')`;
        targetEl.style.backgroundSize='cover';
        targetEl.style.backgroundRepeat='no-repeat';
        targetEl.style.backgroundPosition="center center";

        thumbsEl.appendChild(imageBox);
        thumbsClickHandler(imageBox);
    }
}


/**
* 파일 삭제 처리 콜백
*
*/
function callbackFileDelete(seq) {
    const el = document.getElementById(`file_${seq}`);
    el.parentElement.removeChild(el);

    const thumbs = document.getElementsByClassName("image1_tpl_box");
    if (thumbs.length > 0) {
        thumbs[0].click();
    } else {
        const mainImageEl = document.getElementById("main_images");
        mainImageEl.style.backgroundImage = null;
        mainImageEl.classList.remove("uploaded");
    }

}

/**
* 썸네일 클릭 이벤트 처리
*
*/
function thumbsClickHandler(thumb) {

   const mainImageEl = document.getElementById("main_images");

   thumb.addEventListener("click", function() {
    console.log("클릭!")
    const url = this.dataset.url;
    mainImageEl.style.backgroundImage=`url('${url}')`;
  });
}
```
