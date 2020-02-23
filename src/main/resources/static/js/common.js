/**
 * 公共函数，可在全局调用
 */
//更改昵称
function changeName() {
    swal({
        content: {
            element: "input",
            attributes: {
                placeholder: "请输入昵称",
                type: "text",
            },
        },
    }).then((value) => {
        //删去原有的名片窗口,前提是手机窗口
        if (window.innerWidth <= 600) {
            $(".gray").click();
        }

        var chatterName = value;
        if (null == chatterName || "" == chatterName) {
            return;
        }

        if (chatterName.indexOf(" ") != -1 || chatterName.indexOf("　") != -1) {
            swal("Sad", "昵称不合法，请重新输入", "error");
            return;
        }

        var chatterId = getChatterId();
        // 如果chatterId为空，即还未登录的情况
        if(!chatterId) {
            // 本地修改name
            localStorage.setItem("chatterName", chatterName);
            // 登录
            setChatterName(chatterName);
            createChatter();
            return
        }
        $.ajax({
            url: "/chat/chatter",
            dataType: "json",
            type: "PUT",
            data: {
                "id": chatterId,
                "name": chatterName,
            },
            success: function (result) {
                swal("Nice Guy!", "修改昵称成功！", "success")
                    .then((value) => {
                        setChatterName(chatterName);
                        localStorage.setItem("chatterName", chatterName);
                    });
            },
            error: function (result) {
                console.log(result.responseText);
                var exception = JSON.parse(result.responseText);
                swal("Bad Day", "修改昵称失败，原因是" + exception.msg, "error");
            }
        })
    });;
}

if (window.innerWidth <= 1000) {
    $(".demo").on("click", function () {
        //如果名片打开，则关闭
        if ($(".collapsible-header.active")[0] != null) {
            $("#account_box").click();
        }
    });
}


var isCurrentPage = true;
function listenCurrentPage(){
    var hiddenProperty = 'hidden' in document ? 'hidden' :    
    'webkitHidden' in document ? 'webkitHidden' :    
    'mozHidden' in document ? 'mozHidden' :    
    null;
    var visibilityChangeEvent = hiddenProperty.replace(/hidden/i, 'visibilitychange');
    var onVisibilityChange = function(){
        if (!document[hiddenProperty]) {    
            //console.log('页面激活');
            isCurrentPage = true;
            // 清除未读消息
            document.getElementsByTagName("title")[0].innerText = "chat" ;
        }else{
            //console.log('页面非激活')
            isCurrentPage = false;
        }
    }
    document.addEventListener(visibilityChangeEvent, onVisibilityChange);
}
listenCurrentPage();

