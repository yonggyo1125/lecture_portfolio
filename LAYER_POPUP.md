# 레이어 팝업

## 소스 코드  

> configs/SecurityConfig.java

```java

...

public class SecurityConfig {
    ...
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        ...

        /* 같은 서버 자원 주소는 iframe 허용 처리 */
        http.headers(c -> c.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }
    
    ...

}
```

> resources/static/common/js/layer.js

```javascript
var commonLib = commonLib || {};

commonLib.popup = {
    /**
    * 레이어 팝업 열기
    *
    * @param url : 팝업으로 열 주소
    * @param width : 팝업 너비, 기본값 350
    * @param height : 팝업 높이, 기본값 350
    */
    open(url, width, height) {
        if (!url) return;

        width = width || 350;
        height = height || 350;

        /* 이미 열려 있는 레이어팝업이 있다면 제거 */
        this.close();

        /* 레이어 팝업 요소 생성 S */
        const popupEl = document.createElement("div"); // 팝업
        popupEl.id = "layer_popup";
        popupEl.style.width = width + "px";
        popupEl.style.height = height + "px";

        const iframeEl = document.createElement("iframe");
        iframeEl.width = width;
        iframeEl.height = height;
        iframeEl.src = url;
        popupEl.appendChild(iframeEl);

        /* 레이어 팝업 가운데 배치 좌표 구하기 S */
        const centerX = Math.round((window.innerWidth - width)  / 2);
        const centerY = Math.round((window.innerHeight - height)  / 2);
        popupEl.style.top = centerY + "px";
        popupEl.style.left = centerX + "px";

        /* 레이어 팝업 가운데 배치 좌표 구하기 E */

        const layerDimEl = document.createElement("div"); // 레어어 배경
        layerDimEl.id = "layer_dim";

        /* 레이어 팝업 요소 생성 E */

        /* 레이어 팝업 노출 S */
        document.body.appendChild(layerDimEl);
        document.body.appendChild(popupEl);
        /* 레이어 팝업 노출 E */

        /* 레이어 배경 클릭시 close 처리 */
        layerDimEl.addEventListener("click", this.close);
    },
    /**
    * 레이어 팝업 닫기
    *
    */
    close() {
        const popupEl = document.getElementById("layer_popup");
        if (popupEl) popupEl.parentElement.removeChild(popupEl);

        const layerDimEl = document.getElementById("layer_dim");
        if (layerDimEl) layerDimEl.parentElement.removeChild(layerDimEl);
    }
}
```

> 레이어 팝업은 자주 사용될 수 있으므로 레이아웃 템플릿이 다음과 같이 추가 합니다.
> 
> resources/templates/admin/layouts/main.html
> 
> resources/templates/front/layouts/main.html
>
> resources/templates/mobile/layouts/main.html

```html
...

<th:block layout:fragment="addCss"></th:block>

<script th:src="@{/common/js/layer.js?v={v}(v=${siteConfig.cssJsVersion})}"></script>
<script th:src="@{/common/js/common.js?v={v}(v=${siteConfig.cssJsVersion})}"></script>

...

```

## 레이아웃 템플릿 추가 

> resources/templates/front/layouts/popup.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" th:content="${_csrf.token}">
    <meta name="_csrf_header" th:content="${_csrf.headerName}">
    <meta th:if="${siteConfig.siteDescription != null}" name="description" th:content="${siteConfig.siteDescription}">
    <meta th:if="${siteConfig.siteKeywords != null}" name="keywords" th:content="${siteConfig.siteKeywords}">
    <title>
        <th:block th:if="${pageTitle != null}" th:text="${pageTitle + ' - '}"></th:block>
        <th:block th:if="${siteConfig.siteTitle != null}" th:text="${siteConfig.siteTitle}"></th:block>
    </title>

    <link rel="stylesheet" type="text/css" th:href="@{/common/css/popup_style.css?v={v}(v=${siteConfig.cssJsVersion})}">
    <link rel="stylesheet" type="text/css"
          th:each="cssFile : ${addCommonCss}"
          th:href="@{/common/css/{file}.css?v={v}(file=${cssFile}, v=${siteConfig.cssJsVersion})}">

    <link rel="stylesheet" type="text/css" th:href="@{/front/css/popup_style.css?v={v}(v=${siteConfig.cssJsVersion})}">
    <link rel="stylesheet" type="text/css"
          th:each="cssFile : ${addCss}"
          th:href="@{/front/css/{file}.css?v={v}(file=${cssFile}, v=${siteConfig.cssJsVersion})}">

    <th:block layout:fragment="addCss"></th:block>

    <script th:src="@{/common/js/common.js?v={v}(v=${siteConfig.cssJsVersion})}"></script>
    <script th:each="jsFile : ${addCommonScript}"
            th:src="@{/common/js/{file}.js?v={v}(file=${jsFile}, v=${siteConfig.cssJsVersion})}"></script>

    <script th:src="@{/front/js/common.js?v={v}(v=${siteConfig.cssJsVersion})}"></script>
    <script th:each="jsFile : ${addScript}"
            th:src="@{/front/js/{file}.js?v={v}(file=${jsFile}, v=${siteConfig.cssJsVersion})}"></script>

    <th:block layout:fragment="addScript"></th:block>
</head>
<body>
<main layout:fragment="content"></main>
<iframe name="ifrmProcess" class="dn"></iframe>
</body>
</html>
```

> resources/templates/mobile/layouts/popup.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" th:content="${_csrf.token}">
    <meta name="_csrf_header" th:content="${_csrf.headerName}">
    <meta name="viewport" content="width=device-width, user-scalable=yes, initial-scale=1, minimum-scale=0.5, maximum-scale=3.0">
    <meta th:if="${siteConfig.siteDescription != null}" name="description" th:content="${siteConfig.siteDescription}">
    <meta th:if="${siteConfig.siteKeywords != null}" name="keywords" th:content="${siteConfig.siteKeywords}">
    <title>
        <th:block th:if="${pageTitle != null}" th:text="${pageTitle + ' - '}"></th:block>
        <th:block th:if="${siteConfig.siteTitle != null}" th:text="${siteConfig.siteTitle}"></th:block>
    </title>

    <link rel="stylesheet" type="text/css" th:href="@{/common/css/popup_style.css?v={v}(v=${siteConfig.cssJsVersion})}">
    <link rel="stylesheet" type="text/css"
          th:each="cssFile : ${addCommonCss}"
          th:href="@{/common/css/{file}.css?v={v}(file=${cssFile}, v=${siteConfig.cssJsVersion})}">

    <link rel="stylesheet" type="text/css" th:href="@{/mobile/css/popup_style.css?v={v}(v=${siteConfig.cssJsVersion})}">
    <link rel="stylesheet" type="text/css"
          th:each="cssFile : ${addCss}"
          th:href="@{/mobile/css/{file}.css?v={v}(file=${cssFile}, v=${siteConfig.cssJsVersion})}">

    <th:block layout:fragment="addCss"></th:block>

    <script th:src="@{/common/js/common.js?v={v}(v=${siteConfig.cssJsVersion})}"></script>
    <script th:each="jsFile : ${addCommonScript}"
            th:src="@{/common/js/{file}.js?v={v}(file=${jsFile}, v=${siteConfig.cssJsVersion})}"></script>

    <script th:src="@{/mobile/js/common.js?v={v}(v=${siteConfig.cssJsVersion})}"></script>
    <script th:each="jsFile : ${addScript}"
            th:src="@{/mobile/js/{file}.js?v={v}(file=${jsFile}, v=${siteConfig.cssJsVersion})}"></script>

    <th:block layout:fragment="addScript"></th:block>
</head>
<body>
<main layout:fragment="content"></main>
<iframe name="ifrmProcess" class="dn"></iframe>
</body>
</html>
```

> resources/static/common/css/style.css

```css
...

/* 팝업 공통 S */
#layer_dim { position: fixed; z-index: 100; top: 0; left: 0; width: 100%; height: 100%; background:rgba(0, 0, 0, 0.7); cursor: pointer; }
#layer_popup { position: fixed; z-index: 101; background: #fff; border-radius: 5px; overflow: hidden; }
#layer_popup iframe { border: 0; }
/* 팝업 공통 E */
```

> resources/static/common/css/popup_style.css

```css
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;600;700&display=swap');
* { box-sizing: border-box; outline: none; font-family: 'Noto Sans KR', sans-serif; }
html { font-size: 13px; color: #222; }
body { margin: 0; }
ul { list-style: none; margin: 0; padding: 0; }
li { display: block; }
dl { display: flex; width: 100%; margin: 0; align-items: center; }
dt { width: 130px;  }
dd { width: calc(100% - 130px); padding: 0; margin: 0; }
a { color:#222; text-decoration: none; }

.dn { display: none !important; }

```

## 레이어 팝업 테스트

> test/TestController.java 

```java 
package org.choongang.test;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController implements ExceptionProcessor {

    private final Utils utils;

    @GetMapping("/popup")
    public String popupTest() {

        return utils.tpl("test/popup");
    }
}
```

> resources/templates/front/test/popup.html
>
> /calendar 주소는 달력이 구현되어 있어야 정상적으로 팝업이 동작할 것 입니다. 달력이 필요한 조는 [달력](https://github.com/yonggyo1125/lecture_portfolio/blob/calendar/CALENDAR.md) 소스를 먼저 구현하세요.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/main}">
<main layout:fragment="content">

    <input type="text" id="sdate">

    <script>
        const { popup } = commonLib;
        const sdate = document.getElementById("sdate");
        sdate.onfocus = () => popup.open('/calendar?targetId=sdate', 350, 400);
    </script>
</main>
</html>
```
