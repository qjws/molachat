package com.mola.molachat.service;

import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.dto.ChatterDTO;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @Author: molamola
 * @Date: 19-8-6 下午4:17
 * @Version 1.0
 */
public interface ChatterService {

    /**
     * 创建用户
     * @param chatterDTO
     * @return
     */
    ChatterDTO create(ChatterDTO chatterDTO);

    /**
     * 保存用户
     * @param chatterDTO
     * @return
     */
    ChatterDTO save(ChatterDTO chatterDTO);

    /**
     * 更新用户信息
     * @param chatterDTO
     * @return
     */
    ChatterDTO update(ChatterDTO chatterDTO);

    /**
     * @param chatterDTO
     * @return
     */
    ChatterDTO remove(ChatterDTO chatterDTO);

    /**
     * 列出当前chatter列表
     * @return
     */
    List<ChatterDTO> list();

    /**
     * 根据id查找chatter
     * @param chatterId
     * @return
     */
    ChatterDTO selectById(String chatterId);

    /**
     * 设置chatter的状态
     */
    void setChatterStatus(String chatterId, Integer status);

    /**
     * 获取消息队列
     */
    BlockingQueue<Message> getQueueById(String chatterId);

    /**
     * 消息队列入队
     */
    void offerMessageIntoQueue(Message message,String chatterId);

    /**
     * 添加分数
     */
    void addPoint(String id, Integer point);

}
