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
        return new Promise((resolve, reject) => {
            /* map sdk.js 동적 로드 S */
            const url =`//dapi.kakao.com/v2/maps/sdk.js?appkey=${commonLib.map.appKey}&autoload=false`;

            const xhr = new XMLHttpRequest();
            xhr.open("GET", url)
            xhr.send(null);

            xhr.onreadystatechange = function() {
                if (xhr.status == 200 && xhr.readyState == XMLHttpRequest.DONE) {
                    console.log(Kakao);
                }
            };
        });
        /* map sdk.js 동적 로드 E */


    }
};




