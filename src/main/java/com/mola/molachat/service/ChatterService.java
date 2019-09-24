package com.mola.molachat.service;

import com.mola.molachat.entity.dto.ChatterDTO;

import java.util.List;

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

}
