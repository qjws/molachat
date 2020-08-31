// app全局的一些配置

// ip
var _ip = localStorage.getItem("_ip") ? localStorage.getItem("_ip") : "localhost"
// 端口
var _port = localStorage.getItem("_port") ? localStorage.getItem("_port") :"8550"


function setIp(a) {
    _ip = a
    localStorage.setItem("_ip",_ip)
}

function getIp() {
    return _ip
}

function setPort(a) {
    _port = a;
    localStorage.setItem("_port",_port)
}

function getPort() {
    return _port
}

//获取前缀
function getPrefix() {
    return "https://" + _ip + ":" + _port
}

// 获取socket前缀
function getSocketPrefix() {
    return "wss://" + _ip + ":" + _port
}

// 测试地址合法性
function testHostValid() {
    let url = getPrefix() + "/chat/app/host"
    $.ajax({
        url: url,
        dataType: "json",
        type: "GET",
        xhrFields: {withCredentials:true},
        crossDomain: true,
        success: function (result) {
            return true
        },
        error: function (result) {
            return false
        }
    })
}