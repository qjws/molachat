package com.mola.molachat.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author : molamola
 * @Project: molachat
 * @Description: 判断redis环境是否存在的condition
 * @date : 2020-05-07 21:37
 **/
public class RedisExistCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        return false;
    }
}
