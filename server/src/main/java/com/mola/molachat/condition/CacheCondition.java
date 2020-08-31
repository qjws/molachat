package com.mola.molachat.condition;

import com.mola.molachat.Common.ConditionClassCache;
import com.mola.molachat.utils.EnvUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author : molamola
 * @Project: molachat
 * @Description:
 * @date : 2020-06-14 00:08
 **/
public class CacheCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        // 获取cacheType
        String cacheType = EnvUtils.getEnvProperty(conditionContext, BaseExistCondition.CACHE_TYPE_KEY);
        // 如果cacheType不存在，默认为map缓存
        if (null == cacheType || cacheType.equals(ConditionClassCache.getCache(this.getClass())) ||
                !ConditionClassCache.isCacheTypeExist(cacheType)) {
            return true;
        }
        return false;
    }

}
