package com.mola.molachat.data.impl.redis;

import com.mola.molachat.annotation.RefreshChatterList;
import com.mola.molachat.condition.RedisExistCondition;
import com.mola.molachat.config.SelfConfig;
import com.mola.molachat.data.impl.cache.ChatterFactory;
import com.mola.molachat.entity.Chatter;
import com.mola.molachat.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author : molamola
 * @Project: molachat
 * @Description: 利用redis进行chatter数据持久化
 * @date : 2020-06-08 13:49
 **/
@Component
@Conditional(RedisExistCondition.class)
@Slf4j
public class RedisChatterFactory extends ChatterFactory {

    @Autowired
    private SelfConfig config;

    @Autowired
    private RedisUtil redisUtil;

    private static final String CHATTER_NAMESPACE = "chatter:";

    @PostConstruct
    public void postConstruct(){
        // 从redis中读取list放入缓存
        Set keys = redisUtil.keys(CHATTER_NAMESPACE);
        for (Object key : keys) {
            Chatter chatter = (Chatter) redisUtil.get((String) key);
            super.save(chatter);
        }
    }

    @Override
    public Chatter create(Chatter chatter) {
        Chatter target = super.create(chatter);
        redisUtil.set(CHATTER_NAMESPACE + chatter.getId(), chatter);
        return target;
    }

    @Override
    public Chatter save(Chatter chatter) {
        Chatter target = super.save(chatter);
        redisUtil.set(CHATTER_NAMESPACE + target.getId(), chatter);
        return target;
    }

    @Override
    @RefreshChatterList
    public Chatter update(Chatter chatter) {
        Chatter target = super.update(chatter);
        redisUtil.set(CHATTER_NAMESPACE + target.getId(), chatter);
        return target;
    }

    @Override
    @RefreshChatterList
    public Chatter remove(Chatter chatter) {
        Chatter target = super.remove(chatter);
        redisUtil.del(CHATTER_NAMESPACE + target.getId());
        return target;
    }

    @Override
    public Chatter select(String id) {
        // 取一级缓存
        Chatter firstCache = super.select(id);
        if (null == firstCache) {
            // 取二级缓存
            Object secondCache = redisUtil.get(CHATTER_NAMESPACE + id);
            if (null != secondCache) {
                super.save((Chatter) secondCache);
            }
        }
        return firstCache;
    }

    @Override
    public List<Chatter> list() {
        return super.list();
    }

    @Override
    public BlockingQueue queue(String id) {
        return super.queue(id);
    }

}
