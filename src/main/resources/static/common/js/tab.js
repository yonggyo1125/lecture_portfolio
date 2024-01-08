var widget = widget || {};
widget.tab = {
    /**
    * 탭 내용 불러오기
    *
    * @param url : ajax 요청 URL
    */
    loadContent(url) {
        const contentEl = document.querySelector(".widget_tab .tab_content");
        if (!contentEl) return;

        const { ajaxLoad } = commonLib;

        ajaxLoad("GET", url)
            .then(res => {
                contentEl.innerHTML = res;
            })
            .catch(err => console.log(err));
    }
};

window.addEventListener("DOMContentLoaded", function() {
    const tabs = document.querySelectorAll(".widget_tab .tabs .tab");

    const { loadContent } = widget.tab;
    /* 탭별 클릭 이벤트 처리 S */
    for (const el of tabs) {
      el.addEventListener("click", function() {
        const url = this.value;
        loadContent(url);
      });
   }
   /* 탭별 클릭 이벤트 처리 E */

   /* 첫번째 탭 로드 처리 S */
   tabs[0].click();
});