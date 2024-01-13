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

        const { lat, lng, zoom, selectable } = options;

         // 지도를 표시할 div
         const mapOption = {
                center: new kakao.maps.LatLng(lat ? lat : 33.450701, lng ? lng : 126.570667), // 지도의 중심좌표
                level: zoom ? zoom : 3 // 지도의 확대 레벨
         };

         const map = new kakao.maps.Map(mapContainer, mapOption); // 지도를 생성합니다

         const marker = new kakao.maps.Marker({
             // 지도 중심좌표에 마커를 생성합니다
             position: map.getCenter()
         });
         // 지도에 마커를 표시합니다
         marker.setMap(map);

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
    }
};




