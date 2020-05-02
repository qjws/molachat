package com.mola.molachat.Common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author : molamola
 * @Project: molachat
 * @Description: 版本
 * @date : 2020-03-11 01:42
 **/
@Component
@Data
public class Version {

    @Value("${app.version}")
    private String version;

    public String get() {
        return version;
    }
}
