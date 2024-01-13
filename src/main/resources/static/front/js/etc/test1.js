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