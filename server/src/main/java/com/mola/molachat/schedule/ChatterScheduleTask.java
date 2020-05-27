package com.mola.molachat.schedule;

import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.enumeration.ChatterStatusEnum;
import com.mola.molachat.service.ChatterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : molamola
 * @Project: molachat
 * @Description:
 * @date : 2020-04-30 10:24
 **/
@Component
@Configuration
@EnableScheduling
@Slf4j
public class ChatterScheduleTask {

    private static final float DELETE_RATE = 0.5f;

    @Autowired
    private ChatterService chatterService;

    /**
     * 检查chatter最后在线时间，删除长时间不在线的chatter
     */
    @Scheduled(fixedRate = 60000*10)
    private void deleteChatters() {
        List<ChatterDTO> chatters = chatterService.list();
        // 获得逻辑删除的阈值
        Integer threshold = getDeleteThreshold(chatters).intValue();
        log.info("check:开始检查长时间离线chatter，逻辑删除阈值为：{}",threshold);
        for (ChatterDTO chatter : chatters) {
            Integer point = chatter.getPoint();
            // 获取初始得分
            point *= getInitPoint(chatter);
            Long lastOnline = chatter.getLastOnline();
            // 分数低于阈值且当前不在线
            if (point < threshold && chatter.getStatus() != ChatterStatusEnum.ONLINE.getCode()) {
                chatterService.setChatterStatus(chatter.getId(), ChatterStatusEnum.LOGICAL_DELETE.getCode());
            }
            // 3天不在线，且任然未达到阈值,直接删除
            if (System.currentTimeMillis() - lastOnline > 3*24*60*60*1000){
                if (chatter.getStatus() == ChatterStatusEnum.LOGICAL_DELETE.getCode()) {
                    chatterService.remove(chatter);
                }
            }
        }
    }

    private Float getDeleteThreshold(List<ChatterDTO> chatters) {
        if (chatters.size() == 0) {
            return 0f;
        }
        Integer sum = 0;
        for (ChatterDTO chatterDTO : chatters) {
            Integer point = chatterDTO.getPoint();
            sum += point;
        }
        return sum/chatters.size() * DELETE_RATE;
    }

    private Integer getInitPoint(ChatterDTO chatterDTO) {
        Integer rs = 1;
        if (!chatterDTO.getName().toLowerCase().startsWith("chatter")) {
            rs *= 2;
        }
        if (!chatterDTO.getImgUrl().endsWith("mola.png")) {
            rs *= 2;
        }
        if (!chatterDTO.getSignature().equals("signature")) {
            rs *= 3;
        }
        return rs;
    }
}
