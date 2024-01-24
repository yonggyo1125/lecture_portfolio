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

        console.log(tableEl);
    },
    /**
    * 진열 제거
    *
    */
    removeDisplay() {

    }
};

window.addEventListener("DOMContentLoaded", function() {
    const addDisplayButton = document.querySelector(".add_display");
    // 진열 추가
    addDisplayButton.addEventListener("click", productDisplay.addDisplay);

});