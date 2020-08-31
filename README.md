# molachat
### 一款随处部署的轻量级聊天软件，提供单人聊天，多人聊天，文件传输，音视频通话等功能。
### 提供网页与安卓app两种应用，可实现跨端文件传输、音视频通话。
#### 基于cordova构建移动端app（目前只支持安卓平台）
#### 基于springboot搭建聊天、信令、文件服务器
#### 基于coturn实现NAT穿透，成功率95%
#### 个人主页：www.molapages.xyz
# 配置（在startup.sh中编辑）
|  配置名   | 解释  |
|  ----  | ----  |
| port  | 端口号 |
| connect-timeout  | 检查连接的超时时间 |
| close-timeout  | 断开链接时间 |
| max-client-num  | 最大客户端数量 |
| max-session-message-num  | session最大保存信息数 |
| upload-file-path  | 上传文件保存地址，供下载管理 |
| max-file-size  | 最大存储文件大小,单位为m |
| max-request-size  | 最大请求文件大小,单位为m |
# 如何打开服务
```
git clone  https://github.com/molamolaxxx/molachat.git
cd ./molachat/release/{version}
sh startup.sh
```
访问http://部署服务器的ip:端口/chat
# 如何关闭服务
```
sh shutdown.sh
```
# 我需要什么
Linux发行版、java(>=8)、curl、coturn（如果需要自己搭建stun\turn服务器）
# demo
https://www.molapages.xyz:8550/chat
# app下载
http://www.molapages.xyz/download/molachat.apk

