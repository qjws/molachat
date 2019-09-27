package com.mola.molachat.controller;

import com.mola.molachat.Common.ResponseCode;
import com.mola.molachat.Common.ServerResponse;
import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.entity.dto.SessionDTO;
import com.mola.molachat.enumeration.ChatterStatusEnum;
import com.mola.molachat.exception.service.ChatterServiceException;
import com.mola.molachat.form.ChatterForm;
import com.mola.molachat.server.ChatServer;
import com.mola.molachat.service.ChatterService;
import com.mola.molachat.service.ServerService;
import com.mola.molachat.service.SessionService;
import com.mola.molachat.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.websocket.EncodeException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: molamola
 * @Date: 19-8-7 下午9:12
 * @Version 1.0
 * 创建chatter
 */
@RestController
@RequestMapping("/chatter")
@Slf4j
public class ChatterController {

    @Autowired
    private ChatterService chatterService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private SessionService sessionService;

    @PostMapping
    private ServerResponse create(
                                  @RequestParam("chatterName") String chatterName,
                                  @RequestParam("signature") String signature,
                                  @RequestParam("imgUrl") String imgUrl,
                                  HttpServletRequest request, HttpServletResponse response){

        ChatterDTO chatterDTO = new ChatterDTO();
        chatterDTO.setName(chatterName);
        chatterDTO.setIp(IpUtils.getIp(request));
        chatterDTO.setSignature(signature);
        chatterDTO.setImgUrl(imgUrl);
        chatterDTO.setStatus(ChatterStatusEnum.ONLINE.getCode());

        ChatterDTO result = null;
        try {
            result = chatterService.create(chatterDTO);
            //session中设置id
            request.getSession().setAttribute("id", result.getId());
        } catch (ChatterServiceException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ServerResponse.createByErrorCodeMessage(e.getCode(), e.getMessage());
        }

        return ServerResponse.createBySuccess(result.getId());
    }

    @PutMapping
    private ServerResponse update(@Valid ChatterForm form, BindingResult bindingResult, HttpServletRequest request
                                , HttpServletResponse response) {

        if (bindingResult.hasErrors()){
            log.error("表单验证出错");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ERROR.getCode(),
                    bindingResult.getFieldError().getDefaultMessage());
        }
        //session验证
        if (!checkSession(request, form.getId())){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ERROR.getCode(),
                    "session验证错误");
        }
        ChatterDTO chatterDTO = new ChatterDTO();
        BeanUtils.copyProperties(form, chatterDTO);

        try {
            chatterService.update(chatterDTO);
        } catch (ChatterServiceException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ServerResponse.createByErrorCodeMessage(e.getCode(), e.getMessage());
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * 客户端用来连接测试的url
     * @return
     */
    @GetMapping("/heartBeat")
    private ServerResponse heartBeat(@RequestParam("chatterId") String chatterId, HttpServletRequest request,
                                     HttpServletResponse response){

        //session验证
        if (!checkSession(request, chatterId)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ERROR.getCode(),
                    "session验证错误");
        }
        //设置不缓存,为了在离线时立刻判断
        response.setHeader("Cache-Control","no-cache");
        response.setHeader("Pragma","no-cache");
        response.setDateHeader("Expires",0);

        //检查是否存在某个用户
        ChatterDTO dto = chatterService.selectById(chatterId);
        if (null == dto){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ServerResponse.createByError();
        }
        //判断ip地址是否发生改变，改变则通知重连
        String currentIp = IpUtils.getIp(request);
        if (!currentIp.equalsIgnoreCase(dto.getIp())){
            return ServerResponse.createByErrorMessage("reconnect");
        }

        //设置为在线
        if (dto.getStatus() == ChatterStatusEnum.OFFLINE.getCode()){
            chatterService.setChatterStatus(chatterId, ChatterStatusEnum.ONLINE.getCode());
        }
        //判断当前websocket状态，如果中断连接，则重新初始化
        //ChatServer server = serverService.selectByChatterId(chatterId);

        return ServerResponse.createBySuccess();
    }

    /**
     * 重连机制，用于所有socket失效但网络连接未断的情况的情况
     * @param chatterId
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/reconnect")
    private ServerResponse reconnect(@RequestParam("chatterId") String chatterId, HttpServletRequest request,
                                     HttpServletResponse response){
        //1.判断chatter与server是否都存在
        ChatterDTO chatterDTO = chatterService.selectById(chatterId);
        ChatServer server = serverService.selectByChatterId(chatterId);
        if (null == chatterDTO || null == server){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ServerResponse.createByErrorMessage("重连失败,消息损失");
        }
        //2.判断chatter的id是否与session的相同
        String sessionSavedId = (String) request.getSession().getAttribute("id");
        if (sessionSavedId.equalsIgnoreCase(chatterId)){
            //3.保存session，close掉server，重新创建chatter(内部创建，保持一致)
            List<SessionDTO> saveSessionList = new ArrayList<>();
            for (SessionDTO sessionDTO : sessionService.list()){
                if (sessionDTO.getSessionId().contains(chatterId)){
                    saveSessionList.add(sessionDTO);
                }
            }

            try {
                server.onClose();
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return ServerResponse.createByErrorMessage("重连失败,内部错误");
            }
            //4.保存session,返回成功与chatterId，通知前端重新建立socket
            log.info("数据恢复");
            sessionService.save(saveSessionList);
            //替换chatter数据，只有ip需要更新
            chatterDTO.setIp(IpUtils.getIp(request));
            chatterService.save(chatterDTO);

            return ServerResponse.createBySuccess(chatterId);
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return ServerResponse.createByErrorMessage("重连失败,消息错误");
    }

    private Boolean checkSession(HttpServletRequest request, String chatterId){
        if (chatterId.equalsIgnoreCase((String) request.getSession().getAttribute("id"))){
            return true;
        }
        return false;
    }
}
