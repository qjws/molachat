package com.mola.molachat.data.impl.redis;

import com.mola.molachat.condition.RedisExistCondition;
import com.mola.molachat.config.SelfConfig;
import com.mola.molachat.data.impl.cache.SessionFactory;
import com.mola.molachat.entity.Chatter;
import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.Session;
import com.mola.molachat.entity.VideoSession;
import com.mola.molachat.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

/**
 * @author : molamola
 * @Project: molachat
 * @Description:
 * @date : 2020-06-14 01:30
 **/
@Component
@Conditional(RedisExistCondition.class)
@Slf4j
public class RedisSessionFactory extends SessionFactory{

    @Autowired
    private SelfConfig config;

    @Autowired
    private RedisUtil redisUtil;

    private static final String SESSION_NAMESPACE = "session:";

    @PostConstruct
    public void postConstruct(){
        // 从redis中读取list放入缓存
        Set keys = redisUtil.keys(SESSION_NAMESPACE);
        for (Object key : keys) {
            Session session = (Session) redisUtil.get((String) key);
            super.create(session);
        }
    }

    @Override
    public Session create(Set<Chatter> chatterSet) {
        Session session = super.create(chatterSet);
        redisUtil.set(SESSION_NAMESPACE + session.getSessionId(), session);
        return session;
    }

    @Override
    public Session create(Session session) {
        session = super.create(session);
        redisUtil.set(SESSION_NAMESPACE + session.getSessionId(), session);
        return session;
    }

    @Override
    public Session selectById(String id) {
        // 取一级缓存
        Session firstCache = super.selectById(id);
        if (null == firstCache) {
            // 取二级缓存
            Object secondCache = redisUtil.get(SESSION_NAMESPACE + id);
            if (null != secondCache) {
                super.create((Session) secondCache);
            }
        }
        return firstCache;
    }

    @Override
    public Session remove(Session session) {
        session = super.remove(session);
        redisUtil.del(SESSION_NAMESPACE + session.getSessionId());
        return session;
    }

    @Override
    public List<Session> list() {
        return super.list();
    }

    @Override
    public Message insertMessage(String sessionId, Message message) {
        message = super.insertMessage(sessionId, message);
        redisUtil.set(SESSION_NAMESPACE + sessionId ,super.selectById(sessionId));
        return message;
    }

    @Override
    public VideoSession createVideoSession(String requestChatterId, String acceptChatterId) {
        return super.createVideoSession(requestChatterId, acceptChatterId);
    }

    @Override
    public String removeVideoSession(String chatterId) {
        return super.removeVideoSession(chatterId);
    }

    @Override
    public VideoSession selectVideoSession(String chatterId) {
        return super.selectVideoSession(chatterId);
    }

    @Override
    public List<VideoSession> listVideoSession() {
        return super.listVideoSession();
    }
}
