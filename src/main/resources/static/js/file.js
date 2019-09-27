$(document).ready(function () {

    $chatMsg = $(".chat__messages")[0];

    //发送文件的dom
    // <div class="chat__msgRow"><img src="img/header/15.jpeg" class="contact__photo"
    //         style="float: right; display: inline; margin-right: 0rem;">
    //     <div class="chat__message notMine" style="margin-right: 0.5rem;">
    //         <img src="img/file.svg" style="
    //         width: 6rem;
    //     ">
    //         <div class="progress">
    //             <div class="determinate" style="width: 70%"></div>
    //         </div>
    //         <a style="
    //         display: block;
    //         text-align: center;
    //     ">学习资料.java</a>
    //     </div>
    // </div>
    fileDom = function (filename, isUpload, isMain, uploadId, url) {
        var mainDoc = document.createElement("div");
        $(mainDoc).addClass("chat__msgRow");

        var mainDocChild = document.createElement("div");

        var imgDoc = document.createElement("img");
        if (isMain) {
            //头像img

            imgDoc.src = getChatterImage();
            $(imgDoc).addClass("contact__photo");
            $(imgDoc).css('float', 'right');
            $(imgDoc).css('display', 'inline');
            $(imgDoc).css('margin-right', '0rem');

            $(mainDocChild).css('margin-right', '0.5rem');
            $(mainDocChild).css('text-align', 'center');
            $(mainDocChild).addClass("chat__message notMine");
        }
        else {

            imgDoc.src = getActiveChatter().imgUrl;
            $(imgDoc).addClass("contact__photo");
            $(imgDoc).css('float', 'left');
            $(imgDoc).css('display', 'inline');
            $(imgDoc).css('margin-right', '0rem');

            $(mainDocChild).css('margin-left', '0.5rem');
            $(mainDocChild).css('text-align', 'center');
            $(mainDocChild).addClass("chat__message mine");
        }
        mainDoc.append(imgDoc)

        //添加取消图片
        var cancelImg = document.createElement("img");
        cancelImg.id = "cancel" + uploadId
        cancelImg.src = "img/close-circle.svg"
        $(cancelImg).css("width", "1.2rem")
        $(cancelImg).css("float", "right")
        //添加文件图片
        var fileImg = document.createElement("img");
        fileImg.src = "img/file.svg"
        fileImg.id = "img" + uploadId
        $(fileImg).css("width", "6rem")

        if (isUpload && uploadId != "ready") {
            $(fileImg).css("margin-left", "1.2rem")
            mainDocChild.append(cancelImg)
        }
        mainDocChild.append(fileImg)

        //添加进度条
        if (isUpload) {
            var progress = document.createElement("div");
            $(progress).addClass("progress");
            progressData = document.createElement("div");
            progressData.id = uploadId;
            $(progressData).addClass("determinate");
            if (uploadId == "ready") {
                $(progressData).css("width", "100%")
            } else {
                $(progressData).css("width", "0%")
            }

            progress.append(progressData);
            mainDocChild.append(progress);
        }

        var fileSrc = document.createElement("a");
        $(fileSrc).css("display", "block");
        $(fileSrc).css("text-align", "center");
        fileSrc.innerText = filename;
        fileSrc.href = url;
        fileSrc.target = "_blank";
        fileSrc.id = "src" + uploadId;
        mainDocChild.append(fileSrc);
        mainDoc.append(mainDocChild);
        return mainDoc;
    }

    $("#file_copy").on("click", function () {
        //正在上传的文件id
        rid = ""
        var str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (var i = 0; i < 5; i++) {
            rid += str[Math.round(Math.random() * 61)]
        }

        $("#file-input")[0].click();
        var fileInput = document.querySelector("#file-input");
        fileInput.onchange = function () {
            var file = this.files[0];
            if (file == null) {
                return;
            }
            var form = new FormData();
            var url = "/chat/file/upload";
            var xhr = new XMLHttpRequest();
            form.append("file", file);
            form.append("sessionId", getActiveSessionId());
            form.append("chatterId", getChatterId());

            xhr.open("post", url, true);
            xhr.currentUploadFileId = rid;

            //上传进度事件
            xhr.upload.addEventListener("progress", function (result) {
                if (result.lengthComputable) {
                    //上传进度
                    var percent = (result.loaded / result.total * 100).toFixed(2);
                    console.info(xhr.currentUploadFileId + ":" + percent);
                    if (xhr.currentUploadFileId != null) {
                        $("#" + xhr.currentUploadFileId).css("width", percent + "%")
                    }

                }
            }, false);

            xhr.addEventListener("readystatechange", function () {
                var result = xhr;
                if (result.status != 200) { //error
                    console.log('上传失败', result.status, result.statusText, result.response);
                    //文件名变红
                    $("#src" + xhr.currentUploadFileId).css("color", "rgb(255, 11, 11)");
                    $("#src" + xhr.currentUploadFileId)[0].innerText = file.name + "\n(failed)"
                    //删掉取消键
                    $("#cancel" + xhr.currentUploadFileId).css("display", "none");
                    //恢复文件大小
                    $("#img" + xhr.currentUploadFileId).css("margin-left", "0rem");

                    //swal("error", "上传文件失败", "error");
                }
                else if (result.readyState == 4) { //finished
                    console.log('上传成功', result);
                    //删掉取消键
                    $("#cancel" + xhr.currentUploadFileId).css("display", "none");
                    //恢复文件大小
                    $("#img" + xhr.currentUploadFileId).css("margin-left", "0rem");
                    swal("success", "发送成功！", "success");
                    var url = result.data;
                }
                window.uploadLock = false;
            });
            xhr.send(form); //开始上传
            //上传，锁住
            window.uploadLock = true;

            //添加dom
            var dom = fileDom(file.name, true, true, xhr.currentUploadFileId, "javascript:;");
            $chatMsg.append(dom);
            //滚动
            document.querySelector(".chat__messages").scrollBy({ top: 2500, left: 0, behavior: 'smooth' });
            //设置相关监听器 1.点击取消上传监听 2.鼠标移动放大监听
            $("#cancel" + xhr.currentUploadFileId).on("click", function () {
                swal({
                    title: "Warning",
                    text: "需要终止文件上传吗?",
                    icon: "warning",
                    buttons: true,
                    dangerMode: true,
                }).then((willDelete) => {
                    if (willDelete) {
                        //如果上传已经完毕，则不能取消
                        if ($("#" + xhr.currentUploadFileId).css("width") == $(".progress").css("width")){
                            swal("finish", "上传已经完成，无法取消", "warning")
                            return;
                        }
                        xhr.abort();
                        //删掉取消键
                        $("#cancel" + xhr.currentUploadFileId).css("display", "none");
                        //恢复文件大小
                        $("#img" + xhr.currentUploadFileId).css("margin-left", "0rem");
                        //文件名变黄
                        $("#src" + xhr.currentUploadFileId).css("color", "#e69200");
                        $("#src" + xhr.currentUploadFileId)[0].innerText = file.name + "\n(cancel)"
                        swal("success", "文件已经终止上传", "success");
                    }
                });
            });

            $("#cancel" + xhr.currentUploadFileId).on("mouseover", function () {
                $("#cancel" + xhr.currentUploadFileId).animate({ width: '3rem' });
            });
            $("#cancel" + xhr.currentUploadFileId).on("mouseout", function () {
                $("#cancel" + xhr.currentUploadFileId).animate({ width: '1.2rem' });
            });
        }
    });

});