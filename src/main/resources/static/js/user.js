$(document).ready(function () {
    //常量
    const LIST_MESSAGE = 809;
    const CREATE_SESSION = 122;
    const EXCEPTION = 368;
    const RECIEVE_MESSAGE = 65;
    const HEART_BEAT = 276;

    //唯一用户标识
    var chatterId;
    //唯一用户昵称
    var chatterName = createChatterName();
    //chatter头像
    var chatterImg;

    //flags
    var isLogin = false;

    //socket链接
    var socket;

    //functions
    //如果未登录，给出弹窗
    validAlert = function () {
        if (!isLogin) {
            swal("Welcome!", "欢迎来到mola的聊天室 !")
                .then((value) => {
                $(".collapsible-body").find('p')[0].innerHTML = "<i class='material-icons' style='font-size: 16px;color: #716060;vertical-align: middle;'>account_box</i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + chatterName + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class='material-icons' style='font-size: 16px;color: #716060;vertical-align: middle;' href='javascript:changeName();'>create</a>";
            //头像
            $("img.gravatar")[0].src = (null == localStorage.getItem("imgUrl") ? "img/mola.png" : localStorage.getItem("imgUrl"));

            //弹窗
            popLoginForm();

        });
        }
    }

    popLoginForm = function () {
        setTimeout(function () {
            if (window.innerWidth > 1000) {
                $('.collapsible-header').click();
            }
            createChatter();

        }, 500);
    }

    //创建用户信息，获取chatterId
    createChatter = function () {
        //从本地读取头像链接
        var imgUrl = localStorage.getItem("imgUrl");
        if (null == imgUrl) {
            imgUrl = "img/mola.png";
        }
        $.ajax({
            url: "/chat/chatter",
            dataType: "json",
            type: "post",
            data: {
                "chatterName": chatterName,
                "signature": "signature",
                "imgUrl": imgUrl
            },
            success: function (result) {
                chatterId = result.data;
                //localStorage.setItem("lastId", chatterId)
                //链接到ws服务器
                linkToServer();
                swal("Good Job!", "已成功创建chatter!", "success");
            },
            error: function (result) {
                console.log(result.responseText);
                swal("error", "创建chatter失败,请刷新重试\nCause:" + result.responseText, "error")
            }
        });
    }

    linkToServer = function () {

        if (chatterId == null) {
            swal("error", "未获取chatterId，连接服务器失败!", "error");
            return;
        }
        socket = new WebSocket("ws://" + window.location.hostname + ":8550/chat/server/" + chatterId);

        socket.onopen = function (ev) {
            console.info("socket已经打开");
            console.info(ev);
        };

        socket.onmessage = function (ev) {
            var result = JSON.parse(ev.data)
            if (result.code == LIST_MESSAGE) {
                var chatterList = result.data;
                //刷新聊天列表
                initChatter(chatterList, chatterId);
            }
            else if (result.code == EXCEPTION) {
                swal(result.msg, result.data, "error")
                if (result.msg == "session-invalid") {
                    $(".chat__back")[0].click();
                }
            }
            else if (result.code == CREATE_SESSION) {
                //新建session
                createSession(result.data);
            }
            else if (result.code == RECIEVE_MESSAGE) {
                //收到消息
                receiveMessage(result.data);
            }
            console.info(result);
        };

        socket.onerror = function (ev) {
            console.info("socket出错");
            console.info(ev);
            swal("Sometimes Bad", "服务器出现错误,请刷新", "error", {
                buttons: {
                    catch: {
                        text: "重连",
                        value: "refresh",
                    }
                },
            }).then((value) => {
                reconnect();
        });
        }

        socket.onclose = function (ev) {
            console.info("socket退出");
            console.info(ev);
        }
    }

    //获取随机的chatterName
    function createChatterName() {

        //从存储中读取chatterName
        if (localStorage.getItem("chatterName") != null) {
            return localStorage.getItem("chatterName");
        }

        var str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        var name = "Chatter_";
        for (var i = 0; i < 5; i++) {
            name += str[Math.round(Math.random() * 61)]
        }
        return name;
    }

    //重新连接
    reconnect = function () {
        $.ajax({
            url: "/chat/chatter/reconnect",
            type: "post",
            dataType: "json",
            timeout: 10000,
            data: {
                "chatterId": chatterId
            },
            success: function (result) {
                if (chatterId == result.data) {
                    linkToServer();
                    swal("success", "重连成功", "success")
                }
                else {
                    swal("error", "id不一致，重连失败", "error")
                }
            },
            error: function (result) {
                swal("Sometimes Bad", "重新连接失败,请重连或刷新重试", "error", {
                    buttons: {
                        catch: {
                            text: "重连",
                            value: "refresh",
                        }
                    },
                }).then((value) => {
                    reconnect();
            });
            },
            complete: function (xhr, status) {
                if (status == 'timeout') {
                    // 超时后中断请求
                    xhr.abort();
                    swal("Sometimes Bad", "重新连接超时,请重连或刷新重试", "error", {
                        buttons: {
                            catch: {
                                text: "重连",
                                value: "refresh",
                            }
                        },
                    }).then((value) => {
                        reconnect();
                });
                }
            }
        });
    }

    //判断弹窗是否弹出
    var isPopout = false;
    //发送心跳包
    var timer = setInterval(function () {
        var action = new Object();
        action.code = HEART_BEAT;
        action.msg = "heart-beat";
        action.data = chatterId;
        //未连接时，不发送心跳
        if (null == socket)
            return;
        socket.send(JSON.stringify(action));
        //测试连接url
        $.ajax({
            url: "/chat/chatter/heartBeat",
            type: "get",
            dataType: "json",
            timeout: 10000,
            data: {
                "chatterId": chatterId
            },
            success: function (result) {
                if (result.msg == "reconnect") {
                    console.log("ip改变，需要重连");
                    reconnect();
                }
            },
            error: function (result) {

                // swal("Sometimes Bad", "连接服务器失败，进入离线状态，请等待或刷新重试", "error").then((value) => {

                // });
                if (!isPopout) {
                    isPopout = true;
                    swal("Sometimes Bad", "连接服务器失败，进入离线状态，请重连", "error", {
                        buttons: {
                            catch: {
                                text: "重连",
                                value: "refresh",
                            }
                        },
                    }).then((value) => {
                        reconnect();
                    isPopout = false;
                });
                }
            },
            complete: function (xhr, status) {
                if (status == 'timeout') {
                    // 超时后中断请求
                    xhr.abort();
                    swal("Sometimes Bad", "心跳发送超时,请重连或刷新重试", "error", {
                        buttons: {
                            catch: {
                                text: "重连",
                                value: "refresh",
                            }
                        },
                    }).then((value) => {
                        reconnect();
                });
                }
            }
        });
    }, 10000);

    getSocket = function () {
        return socket;
    }
    getChatterId = function () {
        return chatterId;
    }

    getChatterName = function () {
        return chatterName;
    }

    setChatterName = function (name) {
        chatterName = name;
        $(".collapsible-body").find('p')[0].innerHTML = "<i class='material-icons' style='font-size: 16px;color: #716060;vertical-align: middle;'>account_box</i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + name + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class='material-icons' style='font-size: 16px;color: #716060;vertical-align: middle;' href='javascript:changeName();'>create</a>";
    }

    setChatterImage = function (src) {
        chatterImg = src;
    }

    getChatterImage = function () {
        return chatterImg;
    }

    validAlert();

});