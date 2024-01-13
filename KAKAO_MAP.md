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
    <script th:src="${#strings.concat('//dapi.kakao.com/v2/maps/sdk.js?appkey=', @utils.getApiConfig('kakaoJavascriptKey'), '&libraries=services')}"></script>
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

## 지도 편의 메서드 작성

> static/common/js/map.js

```javascript
var commonLib = commonLib || {};

commonLib.map = {
    /**
    * 지도 로드
    *
    * @param mapId : 맵을 로드할 요소 id 속성 값
    * @param width : 지도 너비 -  숫자로 입력된 경우 px 단위
    * @param height : 지도 높이 - 숫자로 입력된 경우 px 단위
    * @param options : 설정 옵션
    *               lat : 위도
    *               lng : 경도
    *               zoom : 지도 확대 레벨
    *               selectable : 마커 선택 가능 여부
    *               address : 주소,
    *               title : 마커에 올리면 나오는 문구
    *               content : 인포 윈도우에 표현될 내용으로 HTML 문자열이나 dom 가능
    *               image : 마커 이미지,
    *               positions : 여러개 마커 표기시, 이때는 출력만 가능한 용도로 사용
    *
    *
    * @param callback : 지도 로드 후 마커 클릭시 콜백 함수
    *                 : 위도(lat), 경도(lng) 정보를 넘겨줍니다.
    */
    load(mapId, width, height, options, callback) {

        options = options || {};

        const mapContainer = document.getElementById(mapId);
        width = typeof width == 'number' ? `${width}px` : width;
        height = typeof height == 'number' ? `${height}px` : height;

        mapContainer.style.width = width;
        mapContainer.style.height = height;

        /* 옵션 추출 */
        const { lat, lng, zoom, selectable, address, title, content, image, positions } = options;

        /* 여러개 마커 표기 처리 S */
        if (positions && positions.length > 0) {

            const mapOption = {
                center: new kakao.maps.LatLng(positions[0].lat, positions[0].lng), // 지도의 중심좌표
                level: zoom ? zoom : 3 // 지도의 확대 레벨
            };

            const map = new kakao.maps.Map(mapContainer, mapOption); // 지도를 생성합니다

            for (const pos of positions) {
                new kakao.maps.Marker({
                    map, // 마커를 표시할 지도
                    position: new kakao.maps.LatLng(pos.lat, pos.lng), // 마커를 표시할 위치
                    title : pos.title, // 마커의 타이틀, 마커에 마우스를 올리면 타이틀이 표시됩니다
                    image : pos.image && pos.image.length >= 3 ? new kakao.maps.MarkerImage(pos.image[0], new kakao.maps.Size(pos.image[1], pos.image[2])) : undefined // 마커 이미지
                });

                // 인포 윈도우 있는 경우
                if (pos.content && pos.content.trim()) {
                    infoWindow(map, pos.lat, pos.lng, pos.content);
                }
            }
            return;
        }
        /* 여러개 마커 표기 처리 E */

         // 지도를 표시할 div
         const mapOption = {
                center: new kakao.maps.LatLng(lat ? lat : 33.450701, lng ? lng : 126.570667), // 지도의 중심좌표
                level: zoom ? zoom : 3 // 지도의 확대 레벨
         };

         const map = new kakao.maps.Map(mapContainer, mapOption); // 지도를 생성합니다


         let marker = new kakao.maps.Marker({
             // 지도 중심좌표에 마커를 생성합니다
             position: map.getCenter(),
             title,
             image : image && image.length >= 3 ? new kakao.maps.MarkerImage(image[0], new kakao.maps.Size(image[1], image[2])) : undefined,
         });

         // 지도에 마커를 표시합니다
         if (address && address.trim()) {
            // 주소-좌표 변환 객체를 생성합니다
            const geocoder = new kakao.maps.services.Geocoder();

            // 주소로 좌표를 검색합니다
            geocoder.addressSearch(address, function(result, status) {
                // 정상적으로 검색이 완료됐으면
                 if (status === kakao.maps.services.Status.OK) {
                    const lat = result[0].y;
                    const lng = result[0].x;
                    const coords = new kakao.maps.LatLng(lat, lng);

                    // 결과값으로 받은 위치를 마커로 표시합니다
                    marker = new kakao.maps.Marker({
                        map,
                        title,
                        position: coords,
                        image : image && image.length >= 3 ? new kakao.maps.MarkerImage(image[0], new kakao.maps.Size(image[1], image[2])) : undefined,
                    });

                    map.setCenter(coords)

                    /**
                     * 콜백 함수가 있는 경우 후속 처리를 위한 위도, 경도 인자로 호출해 줍니다.
                     *
                     * 위도 : latlng.getLat()
                     * 경도 : latlng.getLng()
                     */
                     if (typeof callback  == 'function') {
                        callback(lat, lng);
                     }

                     // 인포 윈도우 있는 경우
                     if (content && content.trim()) {
                        infoWindow(map, lat, lng, content);
                     }
                } // endif
            }); // endif

         } else {
            marker.setMap(map);

            // 인포 윈도우 있는 경우
            if (content && content.trim()) {
                infoWindow(map, lat, lng, content);
            }
         }

        if (selectable) { // 마커 선택 가능 여부 속성이 있는 경우
             // 지도에 클릭 이벤트를 등록합니다
             // 지도를 클릭하면 마지막 파라미터로 넘어온 함수를 호출합니다
             kakao.maps.event.addListener(map, 'click', function(mouseEvent) {

                 // 클릭한 위도, 경도 정보를 가져옵니다
                 var latlng = mouseEvent.latLng;

                 // 마커 위치를 클릭한 위치로 옮깁니다
                 marker.setPosition(latlng);


                /**
                 * 콜백 함수가 있는 경우 후속 처리를 위한 위도, 경도 인자로 호출해 줍니다.
                 *
                 * 위도 : latlng.getLat()
                 * 경도 : latlng.getLng()
                 */
                if (typeof callback  == 'function') {
                    callback(latlng.getLat(), latlng.getLng());
                }
             });
        }


        /**
        * 인포윈도우 표시
        *
        */
        function infoWindow(map, lat, lng, content) {
            new kakao.maps.InfoWindow({
                map,
                position : new kakao.maps.LatLng(lat, lng),
                content,
                removable : true
            });
        }
    }
};
```

## 테스트

> api/controllers/MapApiController.java

```java 
package org.choongang.api.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/map/api")
@RequiredArgsConstructor
public class MapApiController {

    private final Utils utils;

    /**
     * 클릭한 위치에 마커 표시하고 좌표 얻기
     *
     * @return
     */
    @GetMapping("/test1")
    public String index(Model model) {

        model.addAttribute("addCommonScript", new String[] { "map" });
        model.addAttribute("addScript", new String[] { "etc/test1"} );

        return utils.tpl("etc/map/test1");
    }
}
```

> resources/templates/front/etc/map/test1.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{front/layouts/main}">
<th:block layout:fragment="addCommonScript">
    <th:block th:replace="~{common/_kakao_map_script::script}"></th:block>
</th:block>
<main layout:fragment="content">
    <h2>마커 한개 표시</h2>
    <div id="map"></div>
    <span id="text"></span>

    <h2>마커 여러개 표시</h2>
    <div id="map2"></div>
    <span id="text2"></span>

</main>
</html>
```

> static/front/js/etc/test1.js

```javascript
window.addEventListener("DOMContentLoaded", function() {
    const { map } = commonLib;

    // 마커 한개 표기 및 선택
    map.load("map", "100%", 400, {
        // lat : 위도, //주소 입력하지 않는 경우
        // lng : 경도, //주소 입력하지 않는 경우
        zoom : 5,
        address : '인천광역시 계양구 임학안로 1번길', // 주소를 입력하면 좌표 검색하여 위도, 경도 추출하여 좌표 출력
        title : "마커에 올리면 나오는 문구",
        content : "출력 내용...", //  인포윈도우에 표시할 내용, HTML  또는 dom 요소 가능
        selectable : true, // false이면 고정 위치, true이면 클릭시 마커 및 좌표 변경
        image: ["https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png", 24, 30], // [마커 이미지 URL, 너비, 높이],
    }, (lat, lng) => {
        const text = document.getElementById("text");
        if (text) text.innerHTML = `<b>위도 : ${lat}, 경도 : ${lng}</b>`;
        console.log(lat, lng)
    });

    // 마커 여러개 표기
    const positions = [
        {
            title: '카카오',
            lat: 33.450705,
            lng: 126.570677,
            content: "인포 윈도우 출력 테스트",
            image: ["https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png", 24, 30], // [마커 이미지 URL, 너비, 높이],
        },
        {
            title: '생태연못',
            lat: 33.450936,
            lng: 126.569477,
            image: ["https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png", 24, 30], // [마커 이미지 URL, 너비, 높이],
        },
        {
            title: '텃밭',
            lat: 33.450879,
            lng: 126.569940,
            image: ["https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png", 24, 30], // [마커 이미지 URL, 너비, 높이],
        },
        {
            title: '근린공원',
            lat: 33.451393,
            lng: 126.570738,
            image: ["https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png", 24, 30], // [마커 이미지 URL, 너비, 높이],
        }
    ];
     map.load("map2", "100%", 400, {
        positions,
     });
});
```

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/kakao-map/images/map/image2.png)

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_portfolio/kakao-map/images/map/image3.png)
