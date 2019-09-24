$(document).ready(function () {

    var $friend_list = $(".friend-list")[0];

    //chatterId -> status 1:new Message 0:no message
    var statusMap = new Map();

    //chatterId ->index
    var indexMap;

    /**
     * 返回聊天者的dom
     * @param {*} name 昵称
     * @param {*} url 头像链接
     * @param {*} status　是否为新消息
     * @param {*} intro　个人简介
     */
    chatterDom = function (name, url, hasNewMsg, introText, status) {
        //main
        var mainDoc = document.createElement("div");
        $(mainDoc).addClass("contact");
        //头像
        var imgDoc = document.createElement("img");
        $(imgDoc).addClass("contact__photo");
        imgDoc.src = url;
        //name
        var nameDoc = document.createElement("span");
        $(nameDoc).addClass("contact__name");

        nameDoc.innerText = name;
        //status
        var statusDoc = document.createElement("span");
        $(statusDoc).addClass("contact__status");
        if (hasNewMsg === true && status != 0)
            $(statusDoc).addClass("online");

        if (status == 0) {
            $(statusDoc).addClass("leave");
        }
        //intro
        var intro = document.createElement("span");
        $(intro).addClass("contact_intro");
        intro.innerText = introText;
        //拼接
        mainDoc.append(imgDoc);
        mainDoc.append(nameDoc);
        mainDoc.append(statusDoc);
        mainDoc.append(intro);
        return mainDoc;
    }

    /**
     * idx 第几个联系人
     */
    changeStatus = function (chatterId, isActive) {
        var idx = indexMap.get(chatterId);

        if (isActive) {
            //有未读消息
            console.log("点亮消息,index:" + idx);
            $($(".contact")[idx]).find(".contact__status").addClass("online");
            statusMap.set(chatterId, 1);
        }
        else {
            console.log("熄灭消息,index:" + idx);
            $($(".contact")[idx]).find(".contact__status").removeClass("online");
            statusMap.set(chatterId, 0);
        }
    }

    //初始化聊天者
    initChatter = function (chatterList, selfId) {
        chatterListData = new Array();
        var newStatusMap = new Map();
        indexMap = new Map();
        //indexMap的index
        var index = 0;
        console.log("init_chatterList")

        $(".contact").remove();

        if (chatterList.length == 1) {
            $(".empty").css("display", "block");
        }
        else {
            $(".empty").css("display", "none");
        }

        //判断activechatter是否在list内
        var chatterIsActive = false
        for (var i in chatterList) {

            var chatter = chatterList[i];

            //更改名字时同步更新到聊天界面
            //如果activechatter在，则置为ture
            if (null != getActiveChatter() && chatter.id == getActiveChatter().id) {
                setActiveChatterName(chatter.name);
                //设置聊天clone头像
                setActiveChatterImgUrl(chatter.imgUrl);
                chatterIsActive = true;
            }

            if (chatter.id != selfId) {
                //chatterId到编号的索引
                indexMap.set(chatter.id, index);
                index++;

                //新增的chatter，状态设置为false
                if (statusMap.get(chatter.id) == null || statusMap.get(chatter.id) == 0) {
                    newStatusMap.set(chatter.id, 0);
                    $friend_list.append(chatterDom(chatter.name, chatter.imgUrl, false, chatter.signature, chatter.status));
                }
                //原来有的状态
                else if (statusMap.get(chatter.id) == 1) {
                    newStatusMap.set(chatter.id, 1);
                    $friend_list.append(chatterDom(chatter.name, chatter.imgUrl, true, chatter.signature));
                }
                chatterListData.push(chatter);
            } else {
                setChatterImage(chatter.imgUrl);
            }
        }
        //会话失效
        if (null != getActiveChatter()&&!chatterIsActive) {
            swal("会话已经被重置", "请重新选择会话", "warning")
            $(".chat__back")[0].click();
        }
        //更新状态map
        statusMap = newStatusMap;

        console.log(indexMap);
        //添加点击监听器
        addSessionListener(chatterListData);
    }

    getChatterList = function () {
        return chatterListData;
    }

    getIndexByChatterId = function (chatterId) {
        return indexMap.get(chatterId);
    }

    $(".search__input").bind("keyup", function () {
        var content = $(".search__input")[0].value;
        //判断是否搜索内容为空
        var empty = true;
        $(".contact").each(function () {
            var name = $(this).find(".contact__name")[0].innerText;
            if (name.indexOf(content) == -1) {
                $(this).css("display", "none");
            } else {
                $(this).css("display", "");
                empty = false;
            }
        });
        if (empty) {
            $(".empty").css("display", "");
        } else {
            $(".empty").css("display", "none");
        }
    });
});