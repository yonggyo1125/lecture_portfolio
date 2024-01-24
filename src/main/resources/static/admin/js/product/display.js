const productDisplay = {
    /**
    * 진열 추가
    *
    */
    addDisplay() {
        const num = Date.now();
        let html = document.getElementById("tpl").innerHTML;
        html = html.replace(/\[num\]/g, num);

        const domParser = new DOMParser();
        const dom = domParser.parseFromString(html, "text/html");
        const tableEl = dom.querySelector("table");

        const displayItemsEl = document.getElementById("display_items");
        displayItemsEl.appendChild(tableEl);

        const removeDisplayEl = tableEl.querySelector(".remove_display");
        removeDisplayEl.addEventListener("click", () => productDisplay.removeDisplay(num));
    },
    /**
    * 진열 제거
    *
    */
    removeDisplay(num) {
        const displayEl = document.getElementById(`display_${num}`);
        if (displayEl) displayEl.parentElement.removeChild(displayEl);
    }
};

window.addEventListener("DOMContentLoaded", function() {
    const addDisplayButton = document.querySelector(".add_display");
    // 진열 추가
    addDisplayButton.addEventListener("click", productDisplay.addDisplay);

});


/**
* 상품 선택 후속 처리
*
* @param items : 선택한 상품 정보
* @param target : 선택한 상품을 출력할 요소 id
*/
function callbackPopupSelect(items, target) {
    // items_숫자
    const targetEl = document.getElementById(target);
    const num = target.split("_")[1];
    const tpl = document.getElementById("tpl_item").innerHTML;

    const domParser = new DOMParser();

    for (const item of items) {
        let html = tpl;

        // 이미 진열로 등록된 상품은 건너 뛰기
        if (document.getElementById(`item_${num}_${item.seq}`)) {
            continue;
        }

        html = html.replace(/\[seq\]/g, item.seq)
                    .replace(/\[num\]/g, num)
                    .replace(/\[name\]/g, item.name)
                    .replace(/\[image\]/g,
                        item.listImages.length > 0 ?
                        `<img src='${item.listImages[0].fileUrl}' width='60'>` :
                        '이미지 없음'
                     );
        const dom = domParser.parseFromString(html, 'text/html');
        const itemEl = dom.querySelector(".item");
        targetEl.appendChild(itemEl);

        itemEl.addEventListener("dblclick", function() {
           targetEl.removeChild(itemEl)
        });
    }
}