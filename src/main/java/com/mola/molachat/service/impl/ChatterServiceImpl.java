package com.mola.molachat.service.impl;

import com.mola.molachat.annotation.RefreshChatterList;
import com.mola.molachat.data.impl.ChatterFactory;
import com.mola.molachat.entity.Chatter;
import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.enumeration.ServiceErrorEnum;
import com.mola.molachat.exception.ChatterException;
import com.mola.molachat.exception.service.ChatterServiceException;
import com.mola.molachat.service.ChatterService;
import com.mola.molachat.utils.BeanUtilsPlug;
import com.mola.molachat.utils.CopyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: molamola
 * @Date: 19-8-6 下午4:17
 * @Version 1.0
 */
@Service
@Slf4j
public class ChatterServiceImpl implements ChatterService {

    @Autowired
    private ChatterFactory chatterFactory;

    @Override
    public ChatterDTO create(ChatterDTO chatterDTO) throws ChatterServiceException {

        //1.chatterDTO内部应该包含 :ip, name,默认不为空
        Chatter chatter = new Chatter();
        BeanUtils.copyProperties(chatterDTO, chatter);

        List<Chatter> chatterList = chatterFactory.list();

        //2.检查名称是否重复
        List<String> nameList = chatterList.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());
        if (nameList.contains(chatterDTO.getName())){
            log.info("名称重复");
            throw new ChatterServiceException(ServiceErrorEnum.CHATTER_NAME_DUPLICATE);
        }

        //3.检查ip是否已经注册，默认一个客户端只能登录一个窗口
        List<String> ipList = chatterList.stream()
                .map(e -> e.getIp())
                .collect(Collectors.toList());
        if (ipList.contains(chatterDTO.getIp())){
            log.info("ip已经登录");
            throw new ChatterServiceException(ServiceErrorEnum.CHATTER_IP_DUPLICATE);
        }

        Chatter result = chatterFactory.create(chatter);

        return (ChatterDTO) BeanUtilsPlug.copyPropertiesReturnTarget(result, chatterDTO);
    }

    @Override
    public ChatterDTO update(ChatterDTO chatterDTO) throws ChatterServiceException{

        //1.根据chatterId查找chatter
        Chatter chatter = chatterFactory.select(chatterDTO.getId());
        if (null == chatter){
            //异常
            throw new ChatterServiceException(ServiceErrorEnum.CHATTER_NOT_FOUND);
        }

        //2.如果存在名称，检查名称是否重复
        if (null != chatterDTO.getName()) {
            List<String> nameList = chatterFactory.list().stream()
                    .map(e -> e.getName())
                    .collect(Collectors.toList());
            if (nameList.contains(chatterDTO.getName())) {
                log.info("名称重复");
                throw new ChatterServiceException(ServiceErrorEnum.CHATTER_NAME_DUPLICATE);
            }
        }

        //copy非空值到chatter
        CopyUtils.copyProperties(chatterDTO, chatter);

        //存储
        try {
            chatterFactory.update(chatter);
        } catch (ChatterException e) {
            throw new ChatterServiceException(ServiceErrorEnum.UPDATE_CHATTER_ERROR);
        }

        return (ChatterDTO)BeanUtilsPlug.copyPropertiesReturnTarget(chatter, chatterDTO);
    }

    @Override
    public ChatterDTO remove(ChatterDTO chatterDTO) throws ChatterServiceException{

        Chatter chatter = new Chatter();
        BeanUtils.copyProperties(chatterDTO, chatter);
        chatterFactory.remove(chatter);
        return chatterDTO;
    }

    @Override
    public List<ChatterDTO> list() {

        List<Chatter> chatterList = chatterFactory.list();
        List<ChatterDTO> chatterDTOList = chatterList.stream().map(e -> (ChatterDTO)BeanUtilsPlug.copyPropertiesReturnTarget(e, new ChatterDTO()))
                .collect(Collectors.toList());

        return chatterDTOList;
    }

    @Override
    public ChatterDTO selectById(String chatterId) {

        Chatter chatter = chatterFactory.select(chatterId);
        if (null == chatter)
            return null;
        ChatterDTO result = (ChatterDTO) BeanUtilsPlug.copyPropertiesReturnTarget(chatter, new ChatterDTO());

        return result;
    }

    @Override
    @RefreshChatterList
    public void setChatterStatus(String chatterId, Integer status) {
        Chatter chatter = chatterFactory.select(chatterId);
        if (null == chatter){
            throw new ChatterServiceException(ServiceErrorEnum.CHATTER_NOT_FOUND);
        }
        chatter.setStatus(status);

        chatterFactory.update(chatter);
    }

    @Override
    public ChatterDTO save(ChatterDTO chatterDTO) {
        chatterFactory.save((Chatter)BeanUtilsPlug.copyPropertiesReturnTarget(chatterDTO, new Chatter()));
        return chatterDTO;
    }
}
