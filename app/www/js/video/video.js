// 视频和语音通话
// 基于webrtc
$(document).ready(function() {

    var engines = {
        // 视频引擎
        videoEngine : VideoEngine(),
        // rtc引擎
        rtcEngine : RTCEngine(function(event){
            // onmessage回调
            console.log(event)
        },function(remoteStream) {
            // onaddstream 收到对方的视频流数据
            var video = $("#videoOther")[0];
            video.srcObject = remoteStream;
            video.onloadedmetadata = function(e) {
                video.play();
                removeSpinner()
            };
        },function() {
            setTimeout(function() {
            // onconnected
            var stream = $("#videoSelf")[0].srcObject;
                if (stream) {
                    getEngines().rtcEngine.sendStream(stream)
                } else {
                    console.log("no local stream avaliable")
                }
            },500)
            
        })
    }

    var requestCode = {
        // actionCode:
        VIDEO_REQUEST: 378,
        // 发起视频请求
        REQUEST_VIDEO_ON: 1269,
        // 取消视频请求\挂断
        REQUEST_VIDEO_OFF: 1272,
        // 信令交换
        SIGNALLING_CHANGE: 1483,
        // 取消通话请求
        REQUEST_CANCEL: 1485
    }
    var responseCode = {
        // actionCode:
        VIDEO_RESPONSE: 379,
        // 同意请求响应
        RESPONSE_ACCEPT: 2270,
        // 拒绝请求响应
        RESPONSE_REFUSE: 2271,
    }
    // 模态框
    var $modal = $('#video-modal')
    // 挂断按键
    var $off = $('#video-off')
    // 前置摄像头开关
    var $mirror = $('#open-mirror')
    // 前置摄像头
    var $videoSelf = $('#videoSelf')
    // 返回通话按键
    var $back = $("#back-to-video")
    // 取消请求键
    var $cancel = $("#cancel-request")
    // 全部video的状态对象
    var state = {
        // 是否打开前置摄像头
        openMirror: true,
        // 对方的id
        remoteChatterId: null       
    }
    /*
     dom操作
     */
    // 模态框初始化
    $modal.modal({
        dismissible: true, // Modal can be dismissed by clicking outside of the modal
        opacity: .2, // Opacity of modal background
        in_duration: 300, // Transition in duration
        out_duration: 200, // Transition out duration
        starting_top: '4%', // Starting top style attribute
        ending_top: '100%', // Ending top style attribute
        ready: function(modal, trigger) { // Callback for Modal open. Modal and trigger parameters available.
            
        },
        complete: function() { 
            // 判断是否是挂断电话
            if (state.remoteChatterId) {
                // 挂起
                $back.css("display","inline")
                let $toastContent = $('<span style="font-size:14px">视频挂起到后台</span>');
                Materialize.toast($toastContent, 1000)
            } 
            else {
                // 挂断
                $back.css("display","none")
                let $toastContent = $('<span style="font-size:14px">视频已挂断</span>');
                Materialize.toast($toastContent, 1000)
            }
        } 
    });
    // dom初始化位置
    $modal.css("max-width",800)
    if (window.innerWidth > 800) {
        $modal.css("left",(window.innerWidth - 800)/2)
    }
    $off.css("left",($modal.innerWidth() - 56)/2)
    window.onresize = function() {
        $off.css("left",($modal.innerWidth - 56)/2)
        if (window.innerWidth > 800) {
            $modal.css("left",(window.innerWidth - 800)/2)
        }
    }
    

    /*
     监听事件
     */
    $mirror.on('click', function() {
        if (state.openMirror) {
            // 关闭前置
            $videoSelf.animate({opacity:0})
        } else {
            // 打开前置
            $videoSelf.animate({opacity:1})
        }
        state.openMirror = !state.openMirror
    })
    

    videoOff = function() {
        let socket = getSocket()
        let req = {
            code: requestCode.VIDEO_REQUEST,
            msg: "request_video",
            data: {
                videoActionCode: requestCode.REQUEST_VIDEO_OFF,
                toChatterId: state.remoteChatterId,
                fromChatterId: getChatterId()
            }
        }
        state.remoteChatterId = null
        socket.send(JSON.stringify(req))
        // 关闭视频流
        engines.videoEngine.closeCamera(() => $modal.modal('close'))
        // 关闭rtc
        engines.rtcEngine.close()
        
    }
    $off.on('click', videoOff)

    // 用于取消通话的id
    var toCancelId = null;
    $("#video").on('click',function(){
        // 获得当前聊天窗口的chatter
        let activeChatter = getActiveChatter()
        // 判断是否是群聊
        if (activeChatter.id === "temp-chatter"){
            swal("Not Support", "暂且不支持群聊视频通话" , "warning");
            return
        }
        // 判断对方是否在线
        if ($(".cloned")[0].classList.contains("contact__photo__gray")) {
            swal("offline", "对方已经离线，无法发起视频通话" , "warning");
            return
        }
        // 正在和其他人通话
        if ($cancel[0].style["display"] === "inline") {
            let $toastContent = $('<span style="font-size:14px">您正在通话中</span>');
            Materialize.toast($toastContent, 1000)
            return
        }
        // 检测自己设备状态
        if (!engines.videoEngine.deviceTest(val=>{},err=>{
            swal("device error", "设备出现问题，请检查权限与设备连接" , "warning")
        })) {
            swal("device error", "设备出现问题，请检查权限与设备连接" , "warning");
            return
        }
        
        toCancelId = activeChatter.id
        
        swal("提示","是否与"+activeChatter.name+"进行视频通话?","info")
        .then(function (value) {
            // 发起视频请求
            if (value) {
                sendVideoRequest()
                $cancel.css("display","inline")
            } else {
                engines.videoEngine.closeCamera(()=>{})
            }
        });
    });
    $back.on('click',function(){
        // 判断对方是否在线
        if (state.remoteChatterId) {
            $modal.modal('open')
        }
    });
    $cancel.on('click', function() {
        swal("提示","是否取消视频通话请求?","info")
        .then(function (value) {
            // 取消视频请求
            if (value) {
                sendCancelRequest()
                $cancel.css("display","none")
                engines.videoEngine.closeCamera(() => {})
            }
        });
    })

    /*
     api
     */
    // 发起视频请求
    sendCancelRequest = function() {
        let socket = getSocket()
        let req = {
            code: requestCode.VIDEO_REQUEST,
            msg: "request_video",
            data: {
                videoActionCode: requestCode.REQUEST_CANCEL,
                toChatterId: toCancelId,
                fromChatterId: getChatterId()
            }
        }
        socket.send(JSON.stringify(req))
        let $toastContent = $('<span style="font-size:14px">已取消视频邀请</span>');
        Materialize.toast($toastContent, 1000)
    }

    // 发起视频请求
    sendVideoRequest = function() {
        let socket = getSocket()
        let req = {
            code: requestCode.VIDEO_REQUEST,
            msg: "request_video",
            data: {
                videoActionCode: requestCode.REQUEST_VIDEO_ON,
                toChatterId: getActiveChatter().id,
                fromChatterId: getChatterId()
            }
        }
        socket.send(JSON.stringify(req))
        let $toastContent = $('<span style="font-size:14px">已发出视频邀请，等待对方响应</span>');
        Materialize.toast($toastContent, 1000)
    }


    // 获取状态
    getState = function() {
        return state
    }

    // 获取引擎
    getEngines = function() {
        return engines
    }

    setRemoteChatterId = function(remote) {
        state.remoteChatterId = remote
    }

    addSpinner = function(id) {
        var spinner = "<div class=\"spinner\">\n"+
            "      <div class=\"rect1\"></div>\n"+
            "      <div class=\"rect2\"></div>\n"+
            "      <div class=\"rect3\"></div>\n"+
            "      <div class=\"rect4\"></div>\n"+
            "      <div class=\"rect5\"></div>\n"+
            "      <div class=\"rect6\"></div>\n"+
            "      <div class=\"rect7\"></div>\n"+
            "      <div class=\"rect8\"></div>\n"+
            "  </div>"
        $("#"+id).append(spinner)
    }

    removeSpinner = function(){
        $(".spinner").remove();
    }
    
})