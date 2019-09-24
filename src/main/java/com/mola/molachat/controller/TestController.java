package com.mola.molachat.controller;

import com.mola.molachat.Common.ServerResponse;
import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.exception.service.ChatterServiceException;
import com.mola.molachat.exception.service.ServerServiceException;
import com.mola.molachat.server.ChatServer;
import com.mola.molachat.service.ChatterService;
import com.mola.molachat.service.ServerService;
import com.mola.molachat.service.SessionService;
import com.mola.molachat.utils.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: molamola
 * @Date: 19-8-7 下午3:27
 * @Version 1.0
 * 测试接口可用性，系统可维护性
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ChatterService chatterService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private SessionService sessionService;

    @GetMapping("/onOpen")
    private ServerResponse onOpen(HttpServletRequest request){
        //1.添加用户
        ChatterDTO chatterDTO = new ChatterDTO();
        chatterDTO.setIp(IpUtils.getIp(request));
        chatterDTO.setName("molamola"+System.currentTimeMillis());

        try {
            chatterService.create(chatterDTO);
        } catch (ChatterServiceException e) {
            return ServerResponse.createByErrorCodeMessage(e.getCode(), e.getMessage());
        }

        //2.添加服务器
        ChatServer chatServer = new ChatServer();
        try {
            serverService.create(chatServer);
        } catch (ServerServiceException e) {
            return ServerResponse.createByErrorCodeMessage(e.getCode(), e.getMessage());
        }

        return ServerResponse.createBySuccess(chatterDTO);
    }

    @GetMapping("/onClose")
    private ServerResponse onClose(@RequestParam("chatterId") String chatterId){

        //1.根据用户id删除所有session
        try {
            sessionService.closeSessions(chatterId);
        } catch (com.mola.molachat.exception.SessionException e) {
            return ServerResponse.createByErrorCodeMessage(e.getCode(), e.getMessage());
        }

        //2.注销用户
        ChatterDTO dto = new ChatterDTO();
        dto.setId(chatterId);
        try {
            chatterService.remove(dto);
        } catch (com.mola.molachat.exception.ChatterException e) {
            return ServerResponse.createByErrorCodeMessage(e.getCode(), e.getMessage());
        }

        //3.移除服务器对象
        ChatServer server = new ChatServer();
        server.setChatterId(chatterId);
        try {
            serverService.remove(server);
        } catch (ServerServiceException e) {
            return ServerResponse.createByErrorCodeMessage(e.getCode(), e.getMessage());
        }
        return ServerResponse.createBySuccess();
    }
}
