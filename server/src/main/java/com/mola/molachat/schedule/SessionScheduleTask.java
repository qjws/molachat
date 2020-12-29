package com.mola.molachat.schedule;

import com.mola.molachat.data.ChatterFactoryInterface;
import com.mola.molachat.data.SessionFactoryInterface;
import com.mola.molachat.entity.Chatter;
import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.RobotChatter;
import com.mola.molachat.entity.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author : molamola
 * @Project: molachat
 * @Description:
 * @date : 2020-04-30 10:33
 **/
@Configuration
@EnableScheduling
@Slf4j
public class SessionScheduleTask {

    @Autowired
    private SessionFactoryInterface sessionFactory;

    @Autowired
    private ChatterFactoryInterface chatterFactory;

    @Scheduled(fixedRate = 60000*10)
    private void clearSomeUselessChatterHistory() {
        log.info("开始清理群聊session中冗余chatter历史信息");
        Session session = sessionFactory.selectById("common-session");
        if (null == session) {
            return;
        }
        Set<Chatter> oldChatterSet = session.getChatterSet();
        synchronized (oldChatterSet) {
            Map<String, Chatter> oldChatterMap = new HashMap<>();
            Set<Chatter> newChatterSet = new HashSet<>();
            for (Chatter chatter : oldChatterSet) {
                if (chatter instanceof RobotChatter) {
                    newChatterSet.add(chatter);
                }
                oldChatterMap.put(chatter.getId(), chatter);
            }

            Set<String> ids = new HashSet<>();
            for (Message message : session.getMessageList()) {
                String chatterIdInMessage = message.getChatterId();
                // 首先从线上chatter中取
                Chatter chatterInMessage = chatterFactory.select(chatterIdInMessage);
                if (null == chatterInMessage) {
                    // 再从历史chatter中取
                    chatterInMessage = oldChatterMap.get(chatterIdInMessage);
                }
                // 如果还是null，则声明失效
                if (null == chatterInMessage) {
                    chatterInMessage = new Chatter();
                    chatterInMessage.setId(chatterIdInMessage);
                    chatterInMessage.setName("该用户已失效");
                    chatterInMessage.setImgUrl("img/mola.png");
                }
                newChatterSet.add(chatterInMessage);
            }

            session.setChatterSet(newChatterSet);
        }
    }
}
