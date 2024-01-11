var commonLib = commonLib || {};

commonLib.map = {
    appKey : "ecb744762773804f037af730d26d0ff4", // 카카오 개발자 센터에서 발급받은 API 키 입력

    /**
    * 지도 로드
    *
    * @param mapId : 맵을 로드할 요소 id 속성 값
    * @param width : 지도 너비 -  숫자로 입력된 경우 px 단위
    * @param height : 지도 높이 - 숫자로 입력된 경우 px 단위
    */
    load(mapId, width, height) {
        /* map sdk.js 동적 로드 S */
        const el = document.getElementById("kakao_api_script");
        if (el) {
            el.parentElement.removeChild(el);
        }

        const script = document.createElement("script");
        script.src=`//dapi.kakao.com/v2/maps/sdk.js?appkey=${commonLib.map.appKey}&autoload=false`;
        script.id="kakao_api_script";

        document.head.insertBefore(script, document.getElementsByTagName("script")[0]);
        /* map sdk.js 동적 로드 E */


        script.addEventListener("load", function() {
            const mapContainer = document.getElementById(mapId);
            width = typeof width == 'number' ? `${width}px` : width;
            height = typeof height == 'number' ? `${height}px` : height;

            mapContainer.style.width = width;
            mapContainer.style.height = height;

            const mapOption = {
                center: new kakao.maps.LatLng(33.450701, 126.570667), // 지도의 중심좌표
                level: 3 // 지도의 확대 레벨
            };

            const map = new kakao.maps.Map(mapContainer, mapOption); // 지도를 생성합니다
       });
    }
};




