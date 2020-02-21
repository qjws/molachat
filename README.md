# molachat
一款随处部署的轻量级聊天软件
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
cd ./molachat/dist
sh startup.sh
```
访问http://部署服务器的ip:端口/chat
# 如何关闭服务
```
sh shutdown.sh
```
# 我需要什么
Linux发行版、java(>=8)、curl
# demo
www.molapages.xyz:8550/chat
# app下载
http://www.molapages.xyz/download/molachat.apk
