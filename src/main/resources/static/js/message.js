$(document).ready(function () {
    var $send = $(".send"),
        $chatInput = $(".chat__input")[0],
        $chatMsg = $(".chat__messages")[0];

    //const
    const SEND_MESSAGE = 595;

    //<img class="contact__photo" src="img/mola.png" style="float: right;display: inline;margin-right: 0rem;">
    //message dom
    messageDom = function (content, isMain) {
        //拼装dom
        var mainDoc = document.createElement("div");
        $(mainDoc).addClass("chat__msgRow");

        var mainDocChild = document.createElement("div")

        var imgDoc = document.createElement("img");

        if (isMain) {
            //头像img
            
            imgDoc.src = getChatterImage();
            $(imgDoc).addClass("contact__photo");
            $(imgDoc).css('float', 'right');
            $(imgDoc).css('display', 'inline');
            $(imgDoc).css('margin-right', '0rem');

            $(mainDocChild).css('margin-right', '0.5rem');
            $(mainDocChild).addClass("chat__message notMine");
        }
        else {
            
            imgDoc.src = getActiveChatter().imgUrl;
            $(imgDoc).addClass("contact__photo");
            $(imgDoc).css('float', 'left');
            $(imgDoc).css('display', 'inline');
            $(imgDoc).css('margin-right', '0rem');

            $(mainDocChild).css('margin-left', '0.5rem');
            $(mainDocChild).addClass("chat__message mine");
        }

        mainDoc.append(imgDoc);
        mainDocChild.innerHTML = twemoji.parse(content,{"folder":"svg","ext":".svg","base":"asset/","size":15});
        
        mainDoc.append(mainDocChild);

        return mainDoc;
    }

    //增加一条我方记录
    addMessage = function ($chatMsg, content, isMain) {

        //拼装dom
        var mainDoc = messageDom(content, isMain);

        //添加dom
        $chatMsg.append(mainDoc);

        //滚动
        document.querySelector(".chat__messages").scrollBy({ top: 2500, left: 0, behavior: 'smooth' });
    }

    //删除所有记录
    deleteAllMessage = function () {
        $chatMsg.remove();
    }

    $(".chat__input").bind("keyup", function (ev) {
        if (ev.keyCode == "13") {
            $send.click();
        }
    });

    $(document).on("click", ".send", function () {
        //获取文本框内容
        var content = $chatInput.value;
        if (content === "") {
            swal("stop!","输入不能为空","warning");
            return;
        }

        //清空文本框
        $chatInput.value = "";

        //显示在屏幕上，滚动
        addMessage($chatMsg, content, true);

        //获取socket
        var socket = getSocket();
        //构建message对象
        var action = new Object();
        action.code = SEND_MESSAGE;
        action.msg = "ok";
        var data = new Object();
        data.chatterId = getChatterId();
        data.sessionId = getActiveSessionId();
        data.content = content;
        action.data = data;

        socket.send(JSON.stringify(action));
    });
});