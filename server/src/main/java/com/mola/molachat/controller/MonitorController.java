package com.mola.molachat.controller;

import com.mola.molachat.Common.ServerResponse;
import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.service.ChatterService;
import com.mola.molachat.service.ServerService;
import com.mola.molachat.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: molamola
 * @Date: 19-8-6 下午6:22
 * @Version 1.0
 * 监控chat系统的controller
 */
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    private ChatterService chatterService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ServerService serverService;

    /**
     * 查看在线的chatter
     * @return
     */
    @GetMapping("/chatterList")
    private ServerResponse catChatterList(){

        List<ChatterDTO> chatterDTOS = chatterService.list();

        return ServerResponse.createBySuccess(chatterDTOS);
    }

    @GetMapping("/sessionList")
    private ServerResponse catCurrentSessionList(){

        return ServerResponse.createBySuccess(sessionService.list());
    }

    @GetMapping("/serverList")
    private ServerResponse catServerList(){

        List<String> serverNameList = serverService.list()
                .stream().map(e -> e.getChatterId()).collect(Collectors.toList());
        return ServerResponse.createBySuccess(serverNameList);
    }

}
