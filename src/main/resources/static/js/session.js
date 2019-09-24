$(document).ready(function () {

    //const

    const CREATE_SESSION = 220;
    //当前正通信的chatter
    var activeChatter;

    //当前通信使用的session
    var activeSession;

    var chatterListDoms = document.getElementsByClassName("contact");


    addSessionListener = function (chatterListData) {
        console.log("添加了监听器");
        for (var i = 0; i < chatterListDoms.length; i++) {
            var dom = chatterListDoms[i];
            dom.index = i;
            dom.addEventListener("click", function () {
                //获取当前chatter
                activeChatter = chatterListData[this.index];
                
                //判断是否为离线状态
                if (activeChatter.status == 0){
                    swal("leave","对方离开了，可以继续发送消息，但未必能得到回复","warning");
                }
                //获取session
                var socket = getSocket();
                var action = new Object();
                action.code = CREATE_SESSION;
                action.msg = "ok";
                action.data = getChatterId() + ";" + activeChatter.id;
                //向服务器发送数据
                socket.send(JSON.stringify(action));
                //设置消息已读
                changeStatus(activeChatter.id, false);
            });
        }
    }

    $(".chat__back").on("click", function(){
        activeChatter = null;
        activeSession = null;
    });

    //创建session,socket回调
    createSession = function (session) {
        activeSession = session;
        //初始化消息
        //清除dom
        $(".chat__msgRow").remove();
        var messageList = activeSession.messageList;
        for (var i = 0; i < messageList.length; i++) {
            var message = messageList[i];
            var content = message.content;
            var isMain;
            if (message.chatterId == getChatterId()) {
                isMain = true;
            } else {
                isMain = false;
            }
            var dom;
            if (message.content != null){
                dom = messageDom(message.content, isMain);
            }else{
                dom = fileDom(message.fileName,isMain,isMain,"ready","/chat/"+message.url);
            }
            //dom中添加消息
            $(".chat__messages")[0].append(dom);
        }
        setTimeout(function(){
            document.querySelector(".chat__messages").scrollBy({ top: 2500, left: 0, behavior: 'smooth' });
        },1000);
        
        
    }
    //收到消息，回调
    receiveMessage = function (message) {
        //如果是当前session,立即加载到dom中
        if (activeChatter != null && message.chatterId == activeChatter.id){
            //dom中添加消息
            //如果为文件传输
            var dom;
            if (message.content != null){
                dom = messageDom(message.content, false);
            }else{
                dom = fileDom(message.fileName,false,false,"ready","/chat/"+message.url);
            }
            $(".chat__messages")[0].append(dom);
            document.querySelector(".chat__messages").scrollBy({ top: 2500, left: 0, behavior: 'smooth' });
        }
        //如过非当前session，将对应chatter的未读消息提示点亮
        else{
            var senderId = message.chatterId;

            changeStatus(senderId,true);
        }
    }

    getActiveSessionId = function () {
        return activeSession.sessionId;
    }

    getActiveChatter = function(){
        return activeChatter;
    }
    setActiveChatterName = function(name){
        activeChatter.name = name;
        //更新dom
        $(".chat__name")[0].innerText = name;
    }

    setActiveChatterImgUrl = function(imgUrl){
        activeChatter.imgUrl = imgUrl;
        $("img.cloned")[0].src = imgUrl;
    }
});