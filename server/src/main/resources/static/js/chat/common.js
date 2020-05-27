/**
 * 公共函数，可在全局调用
 */
//更改昵称
function changeName() {
    setTimeout(() => {
        document.getElementsByClassName("swal-content__input")[0].value = getChatterName();
    }, 200);
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
                "token": localStorage.getItem("token")
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

// 时间戳格式化
function times(value) {
    var date = new Date(parseInt(value))
    var tt = [date.getFullYear(), date.getMonth() + 1, date.getDate()].join('-') + ' ' + [date.getHours(), date.getMinutes()/10 < 1 ?( "0" + date.getMinutes()) : date.getMinutes() ].join(':');
    return tt;
}


//更改签名
function changeSign() {
    setTimeout(() => {
        let dom =  document.getElementsByClassName("swal-content__input")[0];
        if (getChatterSign() !== 'signature'){
            document.getElementsByClassName("swal-content__input")[0].value = getChatterSign();
        }
    }, 200);
    swal({
        content: {
            element: "input",
            attributes: {
                placeholder: "请输入签名",
                type: "text",
            },
        },
    }).then((value) => {
        //删去原有的名片窗口,前提是手机窗口
        if (window.innerWidth <= 600) {
            $(".gray").click();
        }

        var chatterSign = value;
        if (null == chatterSign || "" == chatterSign) {
            return;
        }

        if (chatterSign.indexOf(" ") != -1 || chatterSign.indexOf("　") != -1) {
            swal("Sad", "签名不能包含空格", "error");
            return;
        }

        var chatterId = getChatterId();
        // 如果chatterId为空，即还未登录的情况
        if(!chatterId) {
            // 本地修改name
            localStorage.setItem("sign", chatterSign);
            // 登录
            setChatterSign(chatterSign);
            createChatter();
            return
        }
        $.ajax({
            url: "/chat/chatter",
            dataType: "json",
            type: "PUT",
            data: {
                "id": chatterId,
                "signature": chatterSign,
                "token": localStorage.getItem("token")
            },
            success: function (result) {
                swal("Nice Guy!", "修改签名成功！", "success")
                    .then((value) => {
                        setChatterSign(chatterSign);
                        localStorage.setItem("sign", chatterSign);
                    });
            },
            error: function (result) {
                console.log(result.responseText);
                var exception = JSON.parse(result.responseText);
                swal("Bad Day", "修改签名失败，原因是" + exception.msg, "error");
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

//通过字节截取string
function cutStrByByte(str,lenLimit){
    var len = 0;
    for (var i=0; i<str.length; i++) {
        var result="";
        var c = str.charCodeAt(i);
        //单字节加1
        if (c>=0&&c<=128) {
            len++;

        }
        else {
            len+=2;
        }
        if(len>lenLimit)
            return str.substring(0,i)+"...";
    }
    return str;
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

