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

            /* 달력 팝업 닫기 */
            if (parent.commonLib && typeof parent.commonLib.popup  != 'undefined' ) {
                const { popup } = parent.commonLib;
                popup.close();
            }
        });
    }
    /* 달력 클릭 이벤트 처리 E */
});