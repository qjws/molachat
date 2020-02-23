package com.mola.molachat.data.impl;

import com.mola.molachat.annotation.RefreshChatterList;
import com.mola.molachat.config.SelfConfig;
import com.mola.molachat.data.ChatterFactoryInterface;
import com.mola.molachat.entity.Chatter;
import com.mola.molachat.enumeration.DataErrorCodeEnum;
import com.mola.molachat.exception.ChatterException;
import com.mola.molachat.utils.IdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author: molamola
 * @Date: 19-8-5 下午3:09
 * @Version 1.0
 * 管理chatter的工厂
 */
@Component
@Slf4j
public class ChatterFactory implements ChatterFactoryInterface {

    @Autowired
    private SelfConfig config;

    /**
     * chatter数据保存
     */
    private static Map<String, Chatter> chatterData;

    /**
     * 初始化
     */
    public ChatterFactory(){
        chatterData = new HashMap<>();
    }

    @Override
    public synchronized Chatter create(Chatter chatter){
        //赋值id
        chatter.setId(IdUtils.getChatterId());

        //判断是否溢出
        if (isOverFlow()){
            throw new ChatterException(DataErrorCodeEnum.CHATTER_OVER_FLOW);
        }
        chatter.setCreateTime(new Date());
        chatterData.put(chatter.getId(), chatter);
        return chatter;
    }

    @Override
    @RefreshChatterList
    public synchronized Chatter update(Chatter chatter){
        chatterData.replace(chatter.getId(), chatter);
        return chatter;
    }

    @Override
    public synchronized Chatter remove(Chatter chatter){
        chatterData.remove(chatter.getId());
        return chatter;
    }

    @Override
    public Chatter select(String id) {
        return chatterData.get(id);
    }

    @Override
    public List<Chatter> list() {
        List<Chatter> resultList = new ArrayList<>();

        for (String key : chatterData.keySet()){
            resultList.add(chatterData.get(key));
        }
        return resultList;
    }

    private Boolean isOverFlow(){
        if (chatterData.size() < config.getMAX_CLIENT_NUM()){
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public Chatter save(Chatter chatter) {
        chatterData.put(chatter.getId(), chatter);
        return chatter;
    }
}
