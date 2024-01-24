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