package com.mola.molachat.controller;

import com.mola.molachat.Common.ServerResponse;
import com.mola.molachat.Common.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : molamola
 * @Project: molachat
 * @Description:
 * @date : 2020-03-11 01:45
 **/
@RequestMapping("/app")
@RestController
public class AppController {

    @Autowired
    private Version appVersion;

    /**
     * 获取整个app的版本
     * @return
     */
    @GetMapping("/version")
    private ServerResponse version() {
        return ServerResponse.createBySuccess(appVersion.get());
    }

    /**
     * 修改版本
     * @return
     */
    @PostMapping("/version/{version}")
    private ServerResponse changeVersion(@PathVariable String version) {
        appVersion.setVersion(version);
        return ServerResponse.createBySuccess();
    }

    /**
     * app端验证服务器地址
     */
    @GetMapping("/host")
    private ServerResponse host() {
        return ServerResponse.createBySuccess();
    }
}
