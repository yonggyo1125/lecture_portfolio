# Ajax 관련 소스 추가 

## ajax 요청 함수 

> resources/static/common/js/common.js

```javascript
var commonLib = commonLib || {};

/**
* ajax 처리
*
* @param method : 요청 메서드 - GET, POST, PUT ...
* @param url : 요청 URL
* @param responseType : json - 응답 결과를 json 변환, 아닌 경우는 문자열로 반환
*/
commonLib.ajaxLoad = function(method, url, params, responseType) {
    method = !method || !method.trim()? "GET" : method.toUpperCase();
    const token = document.querySelector("meta[name='_csrf']").content;
    const header = document.querySelector("meta[name='_csrf_header']").content;
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open(method, url);
        xhr.setRequestHeader(header, token);

        xhr.send(params);
        responseType = responseType?responseType.toLowerCase():undefined;
        if (responseType == 'json') {
            xhr.responseType=responseType;
        }

        xhr.onreadystatechange = function() {
            if (xhr.status == 200 && xhr.readyState == XMLHttpRequest.DONE) {
                const resultData = responseType == 'json' ? xhr.response : xhr.responseText;

                resolve(resultData);
            }
        };

        xhr.onabort = function(err) {
            reject(err);
        };

        xhr.onerror = function(err) {
            reject(err);
        };

        xhr.ontimeout = function(err) {
            reject(err);
        };
    });
}
```

## csrf 토큰 추가 

> ajax 요청시 요청 헤더에 스프링 시큐리티에서 생성하는 csrf 토큰을 함께 전송해야 정상 처리 됩니다. 다음과 같이 추가 합니다.

> templates/admin/layouts/main.html
> 
> templates/front/layouts/main.html
> 
> templates/mobile/layouts/main.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" th:content="${_csrf.token}">
    <meta name="_csrf_header" th:content="${_csrf.headerName}">
    ...
</head>
...
</html>
```