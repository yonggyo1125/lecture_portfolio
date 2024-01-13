# 카카오 지도 API

- [카카오 지도 API 문서](https://apis.map.kakao.com/web/documentation/)
- [카카오 지도 API 샘플소스](https://apis.map.kakao.com/web/sample/)


> Kakao 지도 Javscript API 는 키 발급을 받아야 사용할 수 있습니다.
> 
> 키를 발급받기 위해서는 카카오 계정이 필요합니다.

## 키 발급 방법 

1. [카카오 개발자사이트](https://developers.kakao.com) (https://developers.kakao.com) 접속
2. 개발자 등록 및 앱 생성
3. 웹 플랫폼 추가: 앱 선택 – [플랫폼] – [Web 플랫폼 등록] – 사이트 도메인 등록
4. 사이트 도메인 등록: [웹] 플랫폼을 선택하고, [사이트 도메인] 을 등록합니다. (예: http://localhost:3000)
5. 페이지 상단의 [JavaScript 키]를 지도 API의 appkey로 사용합니다.

> 개발 서버에서 접속할땐 http://localhost:3000 과 같이 사이트 도메인을 등록해도 되나 실 서비스 서버에서는 실제 도메인이 필요합니다. 카카오 맵을 연동하는 조는 도메인 하나를 구입하고 강사에게 연결 요청을 해주시면 됩니다.

## 기본 설정 - API 설정

> 카카오 API 앱키 추가 
> 
> admin/config/controllers/ApiConfig.java

```java
...

public class ApiConfig {
    private String publicOpenApiKey; // 공공 API 인증키

    private String kakaoJavascriptKey; // 카카오 API - 자바스크립트 앱 키
}
```

> resources/templates/admin/config/api.html

```html
<form name="frmSave" method="post" th:action="@{/admin/config/api}" autocomplete="off" th:object="${apiConfig}">
    <h2>공공 API 설정</h2>
    ...

    <h2>카카오 API 앱키</h2>
    <table class="table_cols">
        <tr>
            <th width="180">Javascript 키</th>
            <td>
                <input type="text" name="kakaoJavascriptKey" th:field="*{kakaoJavascriptKey}">
            </td>
        </tr>
    </table>
    
    ...
    
</form>
```

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/kakao-map/images/map/image1.png)


> commons/Utils.java

```java

...

public class Utils {
    
    ...

    /**
     * API 설정 조회
     *
     * @param key
     * @return
     * 
     */
    public String getApiConfig(String key) {
        Map<String, String> config = infoService.get("apiConfig", new TypeReference<Map<String, String>>() {
        }).orElse(null);
        if (config == null) return "";

        return config.getOrDefault(key, "");
    }
}
```

## 카카오 맵 자뱌스크립트 sdk.js 추가 

> resources/templates/common/_execute_script.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="script">
    <script th:src="${#strings.concat('//dapi.kakao.com/v2/maps/sdk.js?appkey=', @utils.getApiConfig('kakaoJavascriptKey'))}"></script>
</th:block>
</html>
```

> resources/templates/admin/layouts/main.html 
>
> resources/templates/front/layouts/main.html
> 
> resources/templates/mobile/layouts/main.html

```html
...

    <th:block layout:fragment="addCss"></th:block>

    <th:block layout:fragment="addCommonScript"></th:block>

    <script th:src="@{/common/js/common.js?v={v}(v=${siteConfig.cssJsVersion})}"></script>

...

```

