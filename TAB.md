# Ajax 이용 탭 형태 내용 치환

> etc/tab/TabConroller.java

```java
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
```

> front/etc/tab/index.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
    xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
    layout:decorate="~{front/layouts/main}">
<main layout:fragment="content">
    <div class="widget_tab">
        <ul class="tabs">
            <li>
                <input type="radio" name="tab" class="tab" value="/etc/tab/content/1" id="widget_tab1" checked>
                <label for="widget_tab1">탭1</label>
            </li>
            <li>
                <input type="radio" name="tab" class="tab" value="/etc/tab/content/2" id="widget_tab2">
                <label for="widget_tab2">탭2</label>

            </li>
            <li>
                <input type="radio" name="tab" class="tab" value="/etc/tab/content/3" id="widget_tab3">
                <label for="widget_tab3">탭3</label>
            </li>
        </ul>
        <div class="tab_content"></div>
    </div>
</main>
</html>
```

> front/etc/tab/content/1.html

```html
<div xmlns:th="http://www.thymeleaf.org">
    <h1>1페이지</h1>
</div>
```

> front/etc/tab/content/2.html

```html
<div xmlns:th="http://www.thymeleaf.org">
    <h1>2페이지</h1>
</div>
```

> front/etc/tab/content/3.html

```html
<div xmlns:th="http://www.thymeleaf.org">
    <h1>3페이지</h1>
</div>
```

> resources/static/common/js/tab.js

```javascript
var widget = widget || {};
widget.tab = {
    /**
    * 탭 내용 불러오기
    *
    * @param url : ajax 요청 URL
    */
    loadContent(url) {
        const contentEl = document.querySelector(".widget_tab .tab_content");
        if (!contentEl) return;

        const { ajaxLoad } = commonLib;

        ajaxLoad("GET", url)
            .then(res => {
                contentEl.innerHTML = res;
            })
            .catch(err => console.log(err));
    }
};

window.addEventListener("DOMContentLoaded", function() {
    const tabs = document.querySelectorAll(".widget_tab .tabs .tab");

    const { loadContent } = widget.tab;
    /* 탭별 클릭 이벤트 처리 S */
    for (const el of tabs) {
      el.addEventListener("click", function() {
        const url = this.value;
        loadContent(url);
      });
   }
   /* 탭별 클릭 이벤트 처리 E */

   /* 첫번째 탭 로드 처리 S */
   tabs[0].click();
});
```

> resources/static/common/css/tab.css

```css
.widget_tab .tabs { list-style: none; padding: 0; margin: 0; display: flex; height: 30px; align-items: center; }
.widget_tab .tabs input[type='radio'] {  display: none; }
.widget_tab .tabs input[type='radio']+label { padding: 8px 20px; background: #ccc; border-radius: 5px; }
.widget_tab .tabs input[type='radio']:checked+label { background: #222; color: #fff; }
.widget_tab .tabs li+li { margin-left: 5px; }

.widget_tab .tab_content { min-height: 300px; border: 1px solid #222; margin-top: 20px; }
```