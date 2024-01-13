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




