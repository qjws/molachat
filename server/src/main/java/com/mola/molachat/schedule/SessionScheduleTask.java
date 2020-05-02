package com.mola.molachat.schedule;

import com.mola.molachat.data.impl.SessionFactory;
import com.mola.molachat.entity.Chatter;
import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author : molamola
 * @Project: molachat
 * @Description:
 * @date : 2020-04-30 10:33
 **/
@Component
@Configuration
@EnableScheduling
@Slf4j
public class SessionScheduleTask {

    @Autowired
    private SessionFactory sessionFactory;

    @Scheduled(fixedRate = 60000*10)
    private void clearSomeUselessChatterHistory() {
        log.info("开始清理群聊session中冗余chatter历史信息");
        Session session = sessionFactory.selectById("common-session");
        if (null == session) {
            return;
        }
        Set<Chatter> oldChatterSet = session.getChatterSet();
        synchronized (oldChatterSet) {
            Set<Chatter> newChatterSet = new HashSet<>();
            Set<String> ids = new HashSet<>();
            for (Message message : session.getMessageList()) {
                ids.add(message.getChatterId());
            }
            for (Chatter chatter : oldChatterSet) {
                if (ids.contains(chatter.getId())) {
                    newChatterSet.add(chatter);
                }
            }
            session.setChatterSet(newChatterSet);
        }
    }
}
