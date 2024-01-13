# 달력

## 소스 코드

> resources/messages/commons.properties

```properties

...

# 달력 공통 
일=일
월=월
화=화
수=수
목=목
금=금
토=토
이전달=이전달
다음달=다음달
년=년
```

> calendar/Calendar.java
>
> <code>@Lazy</code> 애노테이션이 추가된 이유 : Calendar는 항상 객체를 먼저 만들필요는 없고 필요할 때만 로딩하면 되므로 필요할때 한번만 객체 생성할 수 있도록 함
```java
package org.choongang.calendar;

import org.choongang.commons.Utils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Lazy
@Component
public class Calendar {

    /**
     * 달력 데이터
     *
     * 달력을 만들때 가장 중요한 항목 2가지
     * 1. 매 월 1일의 시작 요일 구하기 : 1일이 달력에서 얼마만큼 떨어져 있는지 위치를 구하는 정보로 사용
     *      - java.time 패키지에서 요일은 getDayOfWeek().getValue()로 구할 수 있으나
     *      - 1~7(월~일)로 나오므로 일요일 부터 시작하는 달력이면 7 -> 0으로 변경한다.
     * 2. 매 월의 마지막 일자 구하기 : 28, 29, 30, 31 등 월마다 달라질 수 있는 값, 다음달 1일에서 하루를 현재 달의 마지막 날짜를 구할 수 있음
     */
    public Map<String, Object> getData(Integer _year, Integer _month) {
        int year, month = 0;
        if (_year == null || _month == null) { // 년도와 월 값이 없으면 현재 년도, 월로 고정
            LocalDate today = LocalDate.now();
            year = today.getYear();
            month = today.getMonthValue();
        } else {
            year = _year.intValue();
            month = _month.intValue();
        }

        LocalDate sdate = LocalDate.of(year, month, 1);
        LocalDate eDate = sdate.plusMonths(1L).minusDays(1);
        int sYoil = sdate.getDayOfWeek().getValue(); // 매월 1일 요일

        sYoil = sYoil == 7 ? 0 : sYoil;

        int start = sYoil * -1;

        int cellNum = sYoil + eDate.getDayOfMonth() > 35 ? 42 : 35;


        Map<String, Object> data = new HashMap<>();

        List<String> days = new ArrayList<>(); // 날짜, 1, 2, 3,
        List<String> dates = new ArrayList<>(); // 날짜 문자열 2024-01-12
        List<String> yoils = new ArrayList<>(); // 요일 정보

        for (int i = start; i < cellNum + start; i++) {
            LocalDate date = sdate.plusDays(i);

            int yoil = date.getDayOfWeek().getValue();
            yoil = yoil == 7 ? 0 : yoil; // 0 ~ 6 (일 ~ 토)
            days.add(String.valueOf(date.getDayOfMonth()));
            dates.add(date.toString());
            yoils.add(String.valueOf(yoil));

            data.put("days", days);
            data.put("dates", dates);
            data.put("yoils", yoils);
        }

        // 이전달 년도, 월
        LocalDate prevMonthDate = sdate.minusMonths(1L);
        data.put("prevYear", String.valueOf(prevMonthDate.getYear())); // 이전달 년도
        data.put("prevMonth", String.valueOf(prevMonthDate.getMonthValue())); // 이전달 월

        // 다음달 년도, 월
        LocalDate nextMonthDate = sdate.plusMonths(1L);
        data.put("nextYear", String.valueOf(nextMonthDate.getYear())); // 다음달 년도
        data.put("nextMonth", String.valueOf(nextMonthDate.getMonthValue())); // 다음달 월

        // 현재 년도, 월
        data.put("year", String.valueOf(year));
        data.put("month", String.valueOf(month));

        // 요일 제목
        data.put("yoilTitles", getYoils());

        return data;
    }

    /**
     * 매개변수가 없는 데이터는 현재 일자 기준의 년도, 월로 달력 데이터 생성
     *
     * @return
     */
    public Map<String, Object> getData() {
        return getData(null, null);
    }

    /**
     * 요일 목록
     *
     * @return
     */
    public List<String> getYoils() {

        return Arrays.asList(
                Utils.getMessage("일", "commons"),
                Utils.getMessage("월", "commons"),
                Utils.getMessage("화", "commons"),
                Utils.getMessage("수", "commons"),
                Utils.getMessage("목", "commons"),
                Utils.getMessage("금", "commons"),
                Utils.getMessage("토", "commons")
        );
    }
}
```

> calendar/controllers.java

```java
package org.choongang.calendar.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.calendar.Calendar;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final Calendar calendar;
    private final Utils utils;


    @GetMapping
    public String index(
            @RequestParam(name="year", required = false) Integer year,
            @RequestParam(name="month", required = false) Integer month,
            Model model) {

        Map<String, Object> data = calendar.getData(year, month);
        model.addAllAttributes(data);


        model.addAttribute("addCss", new String[] { "calendar/style"});
        model.addAttribute("addCommonScript", new String[] { "calendar" });

        return utils.tpl("calendar/index");
    }
}
```

> static/common/css/popup_style.css

```css
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;600;700&display=swap');
* { box-sizing: border-box; outline: none; font-family: 'Nanum Gothic', sans-serif; }
html { font-size: 13px; color: #222; }
body { margin: 0; }
ul { list-style: none; margin: 0; padding: 0; }
li { display: block; }
dl { display: flex; width: 100%; margin: 0; align-items: center; }
dt { width: 130px;  }
dd { width: calc(100% - 130px); padding: 0; margin: 0; }
a { color:#222; text-decoration: none; }
```

> static/front/css/popup_style.css

```css
main { padding: 15px; }

```

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
</body>
</html>
```

> resources/templates/front/calendar/index.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/popup}">
<main layout:fragment="content" class="popup_calendar">
<div class="year_month">
    <a th:href="@{/calendar(year=${prevYear}, month=${prevMonth}, targetId=${param.targetId})}" th:text="#{이전달}"></a>
    <span class="current">
            <th:block th:text="${year}"></th:block>
            <th:block th:text="#{년}"></th:block>

            <th:block th:text="${#numbers.formatInteger(month, 2)}"></th:block>
            <th:block th:text="#{월}"></th:block>
        </span>
    <a th:href="@{/calendar(year=${nextYear}, month=${nextMonth}, targetId=${param.targetId})}" th:text="#{다음달}"></a>
</div>
<ul class="yoils">
    <li th:each="yoil, status : ${yoilTitles}" th:text="${yoil}"></li>
</ul>
<ul class="days">
    <li th:each="day, status : ${days}">
        <div th:text="${day}" class="day" th:data-date="${dates[status.index]}"></div>
    </li>
</ul>
</main>
</html>
```

> static/common/js/calendar.js

> 선택한 날짜(예 - 2024-01-13)를 가지고 처리할 작업이 많은 경우 callback 함수 정의 
>  
> 예) function callbackCalendar(date) { ... } 
> 
> 텍스트 또는 날짜 입력 요소에 값이 완성되는 경우 요청 url에 targetId=요소 id명 입력
> 
> 예) /calendar?targetId=sdate 

```javascript 
window.addEventListener("DOMContentLoaded", function() {


    /* 달력 클릭 이벤트 처리 S */
    const days = document.querySelectorAll(".popup_calendar .day");
    for (const el of days) {
        el.addEventListener("click", function() {
            const date = this.dataset.date; // 선택된 날짜

            /* 날짜 후속 처리가 필요한 경우 콜백 함수 정의 및 호출 */
            if (typeof parent.callbackCalendar == 'function') {
                parent.callbackCalendar(date);
            }

            /**
            * 쿼리 스트링 값으로 targetId가 있다면 부모 창에서 해당 id를 가진 input형태의 document 객체를 찾아서
            * 선택한 날짜로 값을 넣어줍니다.
            *
            */
            const params = new URLSearchParams(location.search);
            const targetId = params.get("targetId");
            if (targetId) {
                const targetEl = parent.document.getElementById(targetId);
                if (targetEl) targetEl.value = date;
            }
        });
    }
    /* 달력 클릭 이벤트 처리 E */
});
```

## 사용 방법 예시

> 입력 요소 id로 선택 날짜 완성 방법 
 
```javascript
const { popup } = commonLib;

popup.open('/calendar?targetId=아이디명', 350, 400);
```

> 콜백 함수로 처리


```javascript
const { popup } = commonLib;

popup.open('/calendar', 350, 400);
...

function callbackCalendar(date) {
    ...
} 
```



완성 달력 화면 

