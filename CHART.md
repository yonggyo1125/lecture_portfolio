# Ubit 코인 현황 차트로 출력하기

## chart.js 라이브러리 추가하기 

> 차트 구현이 필요한 페이지에 다음과 같이 추가 

```
<th:block layout:fragment="addCommonScript">
   <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
</th:block>
```

> 예시

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/main}">
<main layout:fragment="content">

    <th:block layout:fragment="addCommonScript">
        <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    </th:block>

    <div id="upbit_cart"></div>
</main>
</html>
```


> Upbit 차트가 필요한 컨트롤러에 resources/static/common/js/upbit.js를 다음과 같이 추가한다.

```java
import java.util.ArrayList;

List<String> addCommonScript = new ArrayList<>();

...
        
addCommonScript.add("upbit");

...

addAttribute("addCommonScript",addCommonScript);
```


