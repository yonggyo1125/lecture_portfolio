window.addEventListener("DOMContentLoaded", function() {
    /* 상세 설명 에디터 로드 S */
    const { loadEditor } = commonLib;

    loadEditor("description", 450)
        .then(editor => window.editor = editor);

    /* 상세 설명 에디터 로드 E */

    /* 이미지 본문 추가 이벤트 처리 S */
    const insertImages = document.getElementsByClassName("insert_image");
    for (const el of insertImages) {
        el.addEventListener("click", function() {
            const parentId = this.parentElement.parentElement.id;
            const url = this.dataset.url;

            insertImage(url);
        });
    }
    /* 이미지 본문 추가 이벤트 처리 E */

});

/**
* 파일 업로드 후속 처리
*
*/
function callbackFileUpload(files) {
    const editorTpl = document.getElementById("editor_tpl").innerHTML;
    const imageTpl = document.getElementById("image1_tpl").innerHTML;

    const domParser = new DOMParser();
    const mainImageEl = document.getElementById("main_files");
    const listImageEl = document.getElementById("list_files");
    const editorImageEl = document.getElementById("editor_files");

    for (const file of files) {
        const location = file.location;
        let targetEl, html;
        switch (location) {
            case "main":
                html = imageTpl;
                targetEl = mainImageEl;
                break;
            case "list":
                html = imageTpl;
                targetEl = listImageEl;
                break;
            default :
                html = editorTpl;
                targetEl = editorImageEl;
                insertImage(editor, file.fileUrl); // 에디터에 이미지 추가
        }

         /* 템플릿 데이터 치환 S */
         html = html.replace(/\[seq\]/g, file.seq)
                    .replace(/\[fileName\]/g, file.fileName)
                    .replace(/\[imageUrl\]/g, file.fileUrl);

         const dom = domParser.parseFromString(html, "text/html");
         const fileBox = location == 'editor' ? dom.querySelector(".file_tpl_box") :  dom.querySelector(".image1_tpl_box")
         console.log(fileBox);
         targetEl.appendChild(fileBox);


         const el = fileBox.querySelector(".insert_image")
         if (el) {
            // 이미지 본문 추가 이벤트
            el.addEventListener("click", () => insertImage(file.fileUrl));
         }
         /* 템플릿 데이터 치환 E */
    }
}


/**
* 에디터에 이미지 추가
*
*/
function insertImage(source) {
    editor.execute('insertImage', { source });
}

/**
* 파일 삭제 후 후속 처리
*
* @param seq : 파일 등록 번호
*/
function callbackFileDelete(seq) {
    const fileBox = document.getElementById(`file_${seq}`);
    fileBox.parentElement.removeChild(fileBox);
}