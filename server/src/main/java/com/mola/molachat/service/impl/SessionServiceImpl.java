package com.mola.molachat.service.impl;

import com.mola.molachat.Common.websocket.WSResponse;
import com.mola.molachat.Common.websocket.video.VideoWSResponse;
import com.mola.molachat.annotation.AddPoint;
import com.mola.molachat.data.ChatterFactoryInterface;
import com.mola.molachat.data.SessionFactoryInterface;
import com.mola.molachat.entity.Chatter;
import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.RobotChatter;
import com.mola.molachat.entity.Session;
import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.entity.dto.SessionDTO;
import com.mola.molachat.enumeration.ChatterPointEnum;
import com.mola.molachat.enumeration.ServiceErrorEnum;
import com.mola.molachat.exception.service.SessionServiceException;
import com.mola.molachat.server.ChatServer;
import com.mola.molachat.service.ChatterService;
import com.mola.molachat.service.RobotService;
import com.mola.molachat.service.ServerService;
import com.mola.molachat.service.SessionService;
import com.mola.molachat.utils.BeanUtilsPlug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.EncodeException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @Author: molamola
 * @Date: 19-8-6 上午11:08
 * @Version 1.0
 */
@Service
public class SessionServiceImpl implements SessionService{

    @Autowired
    private SessionFactoryInterface sessionFactory;

    @Autowired
    private ChatterService chatterService;

    @Autowired
    private ChatterFactoryInterface chatterFactory;

    @Autowired
    private ServerService serverService;

    @Autowired
    private RobotService robotService;



    @Override
    public SessionDTO create(Set<ChatterDTO> chatterDTOSet) {
        //检查是否set中的chatter都存在，不存在返回错误
        for (ChatterDTO chatterDTO : chatterDTOSet){
            if (null == chatterDTO.getId() || null == chatterService.selectById(chatterDTO.getId())){
                throw new SessionServiceException(ServiceErrorEnum.SESSION_CREATE_ERROR);
            }
        }
        Set<Chatter> chatterSet = new HashSet<>();
        for (ChatterDTO dto : chatterDTOSet){
            chatterSet.add((Chatter) BeanUtilsPlug.copyPropertiesReturnTarget(dto, new Chatter()));
        }
        Session session = sessionFactory.create(chatterSet);
        return (SessionDTO) BeanUtilsPlug.copyPropertiesReturnTarget(session, new SessionDTO());
    }

    @Override
    public SessionDTO findSession(String chatterId1, String chatterId2) {

        SessionDTO result = null;
        String sessId1 = chatterId1 + chatterId2;
        String sessId2 = chatterId2 + chatterId1;
        Session session1 = sessionFactory.selectById(sessId1);
        Session session2 = sessionFactory.selectById(sessId2);
        Session session = (null == session1 ? session2 : session1);

        //如果为null 创建session
        if (null == session){
            //创建对象
            ChatterDTO dto1 = new ChatterDTO();
            dto1.setId(chatterId1);
            ChatterDTO dto2 = new ChatterDTO();
            dto2.setId(chatterId2);
            Set<ChatterDTO> dtoSet = new HashSet<>();
            dtoSet.add(dto1);
            dtoSet.add(dto2);
            result = this.create(dtoSet);
        } else {
            result = (SessionDTO) BeanUtilsPlug
                    .copyPropertiesReturnTarget(session, new SessionDTO());
        }
        return result;
    }

    @Override
    public SessionDTO findSession(String sessionId) {
        Session session = sessionFactory.selectById(sessionId);
        if (null == session) {
            return null;
        }
        return (SessionDTO) BeanUtilsPlug
                .copyPropertiesReturnTarget(session, new SessionDTO());
    }

    @Override
    @AddPoint(action = ChatterPointEnum.INTO_COMMON, key = "#chatterId")
    public SessionDTO findCommonSession(String chatterId) {
        // 查询chatter
        Chatter chatter = chatterFactory.select(chatterId);
        if (null == chatter) {
            throw new SessionServiceException(ServiceErrorEnum.SESSION_CREATE_ERROR);
        }
        Session commonSession = sessionFactory.selectById("common-session");
        if (null == commonSession) {
            // 创建公共聊天区域
            commonSession = new Session();
            commonSession.setSessionId("common-session");
            commonSession.setCreateTime(new Date());
            Set<Chatter> chatterSet = new HashSet<>();
            chatterSet.add(chatter);
            commonSession.setChatterSet(chatterSet);
            commonSession.setMessageList(new CopyOnWriteArrayList<>());
            sessionFactory.create(commonSession);
        } else {
            // 扫描可能会引发线程安全问题
            synchronized (commonSession.getChatterSet()) {
                commonSession.getChatterSet().add(chatter);
            }
        }

        return (SessionDTO) BeanUtilsPlug.copyPropertiesReturnTarget(commonSession, new SessionDTO());
    }

    @Override
    public List<SessionDTO> list() {
        List<Session> sessionList = sessionFactory.list();

        List<SessionDTO> sessionDTOS = sessionList.stream().map(e -> (SessionDTO) BeanUtilsPlug
                .copyPropertiesReturnTarget(e, new SessionDTO()))
                .collect(Collectors.toList());

        return sessionDTOS;
    }

    @Override
    public Integer closeSessions(String chatterId) throws SessionServiceException {
        Integer result = 0;
        //当用户关掉页面时，删除所有session
        List<Session> sessionList = sessionFactory.list();

        for (Session session : sessionList){
            if (session.getSessionId().contains(chatterId)){
                try {
                    sessionFactory.remove(session);
                    result ++;
                } catch (com.mola.molachat.exception.SessionException e) {
                    throw new SessionServiceException(ServiceErrorEnum.SESSIONS_CLOSE_ERROR, e.getMessage());
                }
            }
        }
        return result;
    }

    @Override
    @AddPoint(action = ChatterPointEnum.SEND_MESSAGE, key = "#message.chatterId")
    public Message insertMessage(String sessionId, Message message) throws SessionServiceException{
        //1.查询是否存在对应session
        Session session = sessionFactory.selectById(sessionId);
        if (null == session){
            throw new SessionServiceException(ServiceErrorEnum.SESSION_NOT_FOUND);
        }

        //2.向session中插入message
        sessionFactory.insertMessage(session.getSessionId(), message);

        //3.向socket服务器发送消息,找到session内除发送者的所有ws服务器对象
        for (String chatterId : session.getChatterSet().stream().map(e -> e.getId()).collect(Collectors.toList())){
            Chatter chatter = chatterFactory.select(chatterId);
            if (!chatterId.equals(message.getChatterId())){
                // 如果为机器人，进行插槽调用，利用handler解析message
                if (chatter instanceof RobotChatter) {
                    robotService.onReceiveMessage(message, sessionId, (RobotChatter)chatter);
                    continue;
                }
                //构建response,向不同客户端发送
                try {
                    ChatServer server = serverService.selectByChatterId(chatterId);
                    if (null != server) {
                        // todo 改成生产者消费者模型，异步执行
                        server.getSession().getBasicRemote()
                                .sendObject(WSResponse.message("send content!", message));
                    } else {
                        // 将消息存入消息队列
                        chatterService.offerMessageIntoQueue(message,chatterId);
                    }
                } catch (IOException | EncodeException e) {
                    throw new SessionServiceException(ServiceErrorEnum.SEND_MESSAGE_ERROR, e.getMessage());
                }
            }
        }
        return message;
    }

    @Override
    public void save(List<SessionDTO> sessionList) {
        for (SessionDTO dto : sessionList){
            if (null == sessionFactory.selectById(dto.getSessionId())){
                sessionFactory.create((Session) BeanUtilsPlug
                        .copyPropertiesReturnTarget(dto, new Session()));
            }
        }
    }

    @Override
    public void deleteVideoSession(String chatterId) {
        String needToAlert = sessionFactory.removeVideoSession(chatterId);
        // 发消息,挂断视频
        if (null != needToAlert) {
            serverService.sendResponse(needToAlert, VideoWSResponse
                    .requestVideoOff("挂断视频", null));
        }
    }

    @Override
    public void sendMessageAsync(ChatServer server, Message message) {
        
    }
}
