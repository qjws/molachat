package com.mola.molachat.config;

import com.mola.molachat.data.ChatterFactoryInterface;
import com.mola.molachat.entity.Chatter;
import com.mola.molachat.entity.RobotChatter;
import com.mola.molachat.enumeration.ChatterStatusEnum;
import com.mola.molachat.robot.bus.RobotEventBusRegistry;
import com.mola.molachat.service.SessionService;
import com.mola.molachat.utils.BeanUtilsPlug;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author : molamola
 * @Project: molachat
 * @Description:
 * @date : 2020-12-05 19:44
 **/
@Configuration
public class TestRobotConfig {

    @Resource
    private ChatterFactoryInterface chatterFactory;

    @Resource
    private RobotEventBusRegistry robotEventBusRegistry;

    @Resource
    private SessionService sessionService;

    private String testId = "robot1234";

    private String appKey = "robot1234";

    @PostConstruct
    public void initRobot() {
        Chatter chatter = chatterFactory.select(testId);
        RobotChatter robot = null;
        if (null == chatter) {
            robot = new RobotChatter();
            robot.setId(testId);
            robot.setName("测试机器人");
            robot.setSignature("我是一个测试机器人");
            robot.setStatus(ChatterStatusEnum.ONLINE.getCode());
            robot.setImgUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1607179560790&di=ad8bfba7002f7ab46a5642a83a3b3ca6&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fforum%2Fw%3D580%2Fsign%3D580e773405f431adbcd243317b37ac0f%2F50f2f9dde71190ef9c7f0079c71b9d16fffa60dc.jpg");
            robot.setIp("127.0.0.1");
            robot.setAppKey(appKey);
            chatterFactory.create(robot);
        } else {
            robot = (RobotChatter) BeanUtilsPlug.copyPropertiesReturnTarget(chatter, new RobotChatter());
            robot.setAppKey(appKey);
            chatterFactory.remove(chatter);
            chatterFactory.save(robot);
        }
        sessionService.findCommonSession(robot.getId());
    }
}
