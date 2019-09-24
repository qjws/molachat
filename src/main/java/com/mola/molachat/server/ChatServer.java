package com.mola.molachat.server;

import com.alibaba.fastjson.JSONObject;
import com.mola.molachat.Common.MyApplicationContextAware;
import com.mola.molachat.Common.websocket.Action;
import com.mola.molachat.Common.websocket.ActionCode;
import com.mola.molachat.Common.websocket.WSResponse;
import com.mola.molachat.encoder.ServerEncoder;
import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.entity.dto.SessionDTO;
import com.mola.molachat.exception.service.ServerServiceException;
import com.mola.molachat.exception.service.SessionServiceException;
import com.mola.molachat.service.ChatterService;
import com.mola.molachat.service.ServerService;
import com.mola.molachat.service.SessionService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * @Author: molamola
 * @Date: 19-8-6 上午11:11
 * @Version 1.0
 * websocket服务器 , 请求为chatterId
 */
@ServerEndpoint(value = "/server/{chatterId}", encoders = {ServerEncoder.class})
@Component
@Slf4j
@Data
public class ChatServer {

    private ServerService serverService;

    private SessionService sessionService;

    private ChatterService chatterService;

    //当前连接的session
    private Session session;

    //一个server对应一个chatter
    private String chatterId;

    //心跳包,存放最后一次心跳的时间,默认十秒一次
    private Long lastHeartBeat;


    /**
     * websocket线程安全，独立于spring容器,解决依赖注入的问题
     */
    private void initDependencyInjection(){
        if (null == serverService)
            serverService = MyApplicationContextAware.getApplicationContext().getBean(ServerService.class);
        if (null == sessionService)
            sessionService = MyApplicationContextAware.getApplicationContext().getBean(SessionService.class);
        if (null == chatterService)
            chatterService = MyApplicationContextAware.getApplicationContext().getBean(ChatterService.class);
    }
    /**
     * 登录之后，开始连接
     * @param session
     * @param chatterId
     * @throws IOException
     */
    @OnOpen
    public void onOpen(Session session , @PathParam("chatterId") String chatterId) throws IOException, EncodeException{
        //初始化service依赖
        initDependencyInjection();

        log.info("chatterId:"+chatterId+"开始连接");
        this.chatterId = chatterId;
        this.session = session;
        this.lastHeartBeat = System.currentTimeMillis();

        //1.添加服务器
        try {
            //如果存在服务器：重连状态只是更换session,不存在则创建
            if (null == serverService.selectByChatterId(chatterId)){
                serverService.create(this);
            }

        } catch (ServerServiceException e) {

            //发送异常信息
            this.session.getBasicRemote().sendObject(WSResponse
                    .exception("exception", e.getCode()+":"+e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose() throws IOException, EncodeException{
        log.info("chatterId:"+chatterId+"断开连接");

        //1.根据用户id删除所有session
        Integer closeSessionNum = sessionService.closeSessions(chatterId);
        log.info("共关闭"+closeSessionNum+"个session");

        //2.注销用户
        ChatterDTO chatterDTO = new ChatterDTO();
        chatterDTO.setId(chatterId);

        chatterService.remove(chatterDTO);
        //3.移除服务器对象
        serverService.remove(this);

        log.info("成功移除chatter对象");
        log.info("成功移除server对象");

    }

    /**
     * 收到客户端消息,message定义动作与数据,目前动作只可能为传递消息
     * 前端发来的动作，包括
     * 1.发送消息
     * 2.创建会话
     * 3.心跳
     *
     * todo 异常处理
     */
    @OnMessage
    public void onMessage(String actionJSON) throws EncodeException, IOException{

        //解析前端发送的action
        JSONObject jsonObject = JSONObject.parseObject(actionJSON);
        Action action = new Action();
        action.setCode((Integer) jsonObject.get("code"));
        action.setMsg((String) jsonObject.get("msg"));
        action.setData(jsonObject.get("data"));

        //log.info(action.toString());
        switch (action.getCode()){
            case ActionCode.CREATE_SESSION:{
                log.info("action:创建/找到session");
                //按照分号获取id
                String ids = (String) action.getData();
                String[] idSplit = ids.split(";");

                //检查对方server是否失活

                //查找是否已经存在session,没有的话创建session
                SessionDTO sessionDTO = sessionService.findSession(idSplit[0], idSplit[1]);

                //返回session信息
                this.session.getBasicRemote()
                        .sendObject(WSResponse.createSession("ok", sessionDTO));
                break;
            }
            case ActionCode.SEND_MESSAGE:{
                log.info("action:客户端发送消息");
                //发送消息
                //1.解析json 发送者id sessionId
                JSONObject data = (JSONObject) action.getData();
                String chatterId = data.getString("chatterId");
                String content = data.getString("content");

                //2.构建message
                Message message = new Message();
                message.setContent(content);
                message.setChatterId(chatterId);

                //3.调用session
                try {
                    sessionService.insertMessage(data.getString("sessionId"), message);
                } catch (SessionServiceException e) {
                    //发送异常信息
                    e.printStackTrace();
                    this.session.getBasicRemote()
                            .sendObject(WSResponse.exception("session-invalid", "当前会话已经失效"));
                }
                break;
            }

            //心跳,data为id
            case ActionCode.HEART_BEAT : {
                String chatterId = (String) action.getData();
                //log.info("action:客户端发送心跳, id:"+chatterId);
                serverService.setHeartBeat(chatterId);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) throws EncodeException, IOException{
        log.error("chatterId:"+chatterId+"发生错误");
        this.session.getBasicRemote()
                .sendObject(WSResponse.exception("server-error", "服务器出现错误"));
        error.printStackTrace();
    }

}
