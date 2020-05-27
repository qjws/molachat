// 用户相关逻辑
$(document).ready(function() {
    //常量
    const LIST_MESSAGE = 809;
    const CREATE_SESSION = 122;
    const EXCEPTION = 368;
    const RECIEVE_MESSAGE = 65;
    const HEART_BEAT = 276;
    const VIDEO_REQUEST = 378;
    const VIDEO_RESPONSE = 379;

    //唯一用户标识
    var chatterId;
    //唯一用户昵称
    var chatterName = createChatterName();
    // 用户签名
    var chatterSign = getSign();
    //chatter头像
    var chatterImg;

    //flags
    var isLogin = false;

    //socket链接
    var socket;
    // dom
    var $user_info = $(".user_info");
    var $demo = $(".demo");
    var $menu = $("#menu");

    // token 
    var token = localStorage.getItem("token")

    //functions
    //如果未登录，给出弹窗
    validAlert = function() {
        if (!isLogin) {
            var mailContailer = $(".container")[0];
            mailContailer.remove();
            mailContailer.style = null;
            swal({
                title: "Welcome!",
                html: true,
                content: mailContailer,
                className: "none-bg",
                button: false,
            }).then((value) => {
                    $(".collapsible-body").find('p')[0].innerHTML = "<i class='material-icons' style='font-size: 16px;color: #716060;vertical-align: middle;'>account_box</i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + chatterName + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class='material-icons' style='font-size: 16px;color: #716060;vertical-align: middle;' href='javascript:changeName();'>create</a>";
                    //头像
                    $("img.gravatar")[0].src = (null == localStorage.getItem("imgUrl") ? "img/mola.png" : localStorage.getItem("imgUrl"));
                    $(".collapsible-body").find('p')[1].innerHTML = "<a class='material-icons' style='font-size: 14px;color: #716060;' href='javascript:changeSign();'>create</a>&nbsp;" + (chatterSign === "signature" ? "点击修改签名":chatterSign);
                    $alert.removeClass("hidden-bg-line");
                    //弹窗
                    popLoginForm();
                });
            var $alert = $(".swal-overlay")
            $alert.addClass("hidden-bg-line")
        }
    }

    popLoginForm = function() {
        setTimeout(function() {
            if (window.innerWidth > 1000) {
                // 弹窗变成显示状态
                $user_info.css({"display":""})
                $('.collapsible-header').click();
                $(".user_info").animate({ "opacity": 1 })
            }
            // 聊天窗动画渐进
            $demo.animate({"opacity" : 1},800)
            $menu.animate({"opacity" : 1},800)
            recoverChatter()

        }, 500);
    }

    recoverChatter = function() {
        // 先检测有没有残留的chatterId
        var preId = localStorage.getItem("preId");
        $.ajax({
            url: "/chat/chatter/reconnect",
            type: "post",
            dataType: "json",
            timeout: 10000,
            data: {
                "chatterId": preId,
                "token":token
            },
            success: function(result) {
                console.info(preId)
                console.info(preId == result.data)
                if (preId == result.data) {
                    chatterId = preId;
                    linkToServer();
                    swal("Welcome!", "重连成功", "success")
                } else {
                    swal("error", "id不一致，重连失败", "error")
                }
            },
            error: function(result) {
                createChatter()
            },
            complete: function(xhr, status) {
                if (status == 'timeout') {
                    createChatter()
                }
            }
        });
    }

    //创建用户信息，获取chatterId
    createChatter = function() {
        $.ajax({
            url: "/chat/chatter",
            dataType: "json",
            type: "delete",
            data: {
                "preId": localStorage.getItem("preId")
            },
            success: function(result) {
                // 从本地读取头像链接
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
                        "signature": chatterSign,
                        "imgUrl": imgUrl
                    },
                    success: function(result) {
                        chatterId = result.data.id
                        token = result.data.token
                        localStorage.setItem("token", result.data.token)
                        localStorage.setItem("preId", chatterId)
                        //链接到ws服务器
                        linkToServer();
                        swal("Welcome!", "已成功创建chatter!", "success");
                    },
                    error: function(result) {
                        console.log(result.responseText);
                        var exception = JSON.parse(result.responseText);
                        swal("error", "创建chatter失败,请刷新重试，原因是" + exception.msg, "error")
                    }
                });
            }
        });
        
    }

    linkToServer = function() {

        if (chatterId == null) {
            swal("error", "未获取chatterId，连接服务器失败!", "error");
            return;
        }
        socket = new WebSocket("wss://" + window.location.hostname + ":8550" + "/chat/server/" + chatterId);

        socket.onopen = function(ev) {
            console.info("socket已经打开");
            console.info(ev);
        };

        socket.onmessage = function(ev) {
            var result = JSON.parse(ev.data)
            if (result.code == LIST_MESSAGE) {
                var chatterList = result.data;
                //刷新聊天列表
                initChatter(chatterList, chatterId);
            } else if (result.code == EXCEPTION) {
                swal(result.msg, result.data, "error")
                if (result.msg == "session-invalid") {
                    $(".chat__back")[0].click();
                }
            } else if (result.code == CREATE_SESSION) {
                //新建session
                createSession(result.data);
            } else if (result.code == RECIEVE_MESSAGE) {
                //收到消息
                receiveMessage(result.data);
            } else if (result.code == VIDEO_REQUEST) {
                // 视频消息请求
                receiveVideoRequest(result.data)
            } else if (result.code == VIDEO_RESPONSE) {
                // 视频消息返回
                receiveVideoResponse(result.data)
            }
            console.info(result);
        };

        socket.onerror = function(ev) {
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

        socket.onclose = function(ev) {
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

    // 获取签名
    function getSign() {
        //从存储中读取sign
        if (localStorage.getItem("sign") != null) {
            return localStorage.getItem("sign");
        }
        return "signature"
    }

    //重新连接
    reconnect = function() {
        $.ajax({
            url: "/chat/chatter/reconnect",
            type: "post",
            dataType: "json",
            timeout: 10000,
            data: {
                "chatterId": chatterId,
                "token": token
            },
            success: function(result) {
                if (chatterId == result.data) {
                    linkToServer();
                    swal("success", "重连成功", "success")
                } else {
                    swal("error", "id不一致，重连失败", "error")
                }
            },
            error: function(result) {
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
            complete: function(xhr, status) {
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
    var timer = setInterval(function() {
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
                "chatterId": chatterId,
                "token": token
            },
            success: function(result) {
                if (result.msg == "reconnect") {
                    console.log("ip改变，需要重连");
                    reconnect();
                }
                else if(result.msg == "no-server-exist") {
                    console.log("服务器对象被移除");
                    reconnect();
                }
            },
            error: function(result) {
                if (!isPopout) {
                    isPopout = true;
                    swal("Sometimes Bad", "用户已被销毁，请重新创建", "error", {
                        buttons: {
                            catch: {
                                text: "刷新",
                                value: "refresh",
                            }
                        },
                    }).then((value) => {
                        location.reload();
                        isPopout = false;
                    });
                }
                
            },
            complete: function(xhr, status) {
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

    getSocket = function() {
        return socket;
    }
    getChatterId = function() {
        return chatterId;
    }

    getChatterName = function() {
        return chatterName;
    }

    setChatterSign = function(sign) {
        chatterSign = sign;
        $(".collapsible-body").find('p')[1].innerHTML = "<a class='material-icons' style='font-size: 14px;color: #716060;' href='javascript:changeSign();'>create</a>&nbsp;" + chatterSign;
    }

    getChatterSign = function() {
        return chatterSign;
    }

    setChatterName = function(name) {
        chatterName = name;
        $(".collapsible-body").find('p')[0].innerHTML = "<i class='material-icons' style='font-size: 16px;color: #716060;vertical-align: middle;'>account_box</i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + name + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class='material-icons' style='font-size: 16px;color: #716060;vertical-align: middle;' href='javascript:changeName();'>create</a>";
    }

    setChatterImage = function(src) {
        chatterImg = src;
    }

    getChatterImage = function() {
        return chatterImg;
    }

    validAlert();

});