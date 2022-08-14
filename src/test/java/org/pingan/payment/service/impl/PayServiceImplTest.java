package org.pingan.payment.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pingan.payment.dao.mapper.PayOrderDao;
import org.pingan.payment.dao.model.PayOrderPO;
import org.pingan.payment.entity.dto.UnifiedPayDTO;
import org.pingan.payment.service.PayChannelService;
import org.pingan.payment.service.PayChannelServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 指定在spring boot的环境中执行
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PayServiceImpl.class})
@ActiveProfiles("test")
public class PayServiceImplTest {

    /**
     * 目标测试类实例
     */
    @Autowired
    PayServiceImpl payServiceImpl;

    /**
     * 通过Mockito框架的mockbean注解实现模拟redis分布式锁的依赖对象
     */
    @MockBean
    private RedisLockRegistry redisLockRegistry;

    /**
     * 模拟支付账单持久层
     */
    @MockBean
    private PayOrderDao payOrderDao;

    /**
     * 模拟渠道处理工厂类
     */
    @MockBean
    private PayChannelServiceFactory payChannelServiceFactory;

    /**
     * 模拟支付宝渠道处理类对象
     */
    @MockBean
    private PayChannelService aliPayServiceImpl;

    /**
     * 统一支付接口的业务层方法的单元测试
     */
    @Test
    public void unifiedPay() {
        //模拟生成请求参数
        UnifiedPayDTO unifiedPayDTO = new UnifiedPayDTO();
        unifiedPayDTO.setOrderId("2983748237482734");
        unifiedPayDTO.setAppId("100001");
        unifiedPayDTO.setTradeType("topup");
        unifiedPayDTO.setChannel(1);
        unifiedPayDTO.setPayType("ALI_PAY_H5");
        unifiedPayDTO.setAmount(100);
        unifiedPayDTO.setCurrency("CNY");
        unifiedPayDTO.setUserId("1002");
        unifiedPayDTO.setSubject("apple iphone15 plus");
        unifiedPayDTO.setBody("iphone15 plus");
        unifiedPayDTO.setNotifyUrl("http://localhost:9093/notify");
        unifiedPayDTO.setReturnUrl("http://localhost:9093/notify");

        //模拟依赖对象的行为,被测试代码执行时可以得到正常的运行条件,
        // 不需要依赖第三方组件或网络，从而可能让内部代码的逻辑得到测试
        //模拟分布式锁的对象行为
        given(redisLockRegistry.obtain(any(String.class))).willReturn(new Lock() {
            @Override
            public void lock() {

            }

            @Override
            public void lockInterruptibly() throws InterruptedException {

            }

            @Override
            public boolean tryLock() {
                return true;
            }

            @Override
            public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
                return true;
            }

            @Override
            public void unlock() {

            }

            @Override
            public Condition newCondition() {
                return null;
            }
        });

        //1、mock持久层依赖方法执行时返回支付账单模拟数据对象
        given(payOrderDao.selectByMap(any(Map.class))).willReturn(null);

        //2、mock渠道service工厂返回渠道处理实例对象
        given(payChannelServiceFactory.createPayChannelService(any(Integer.class))).willReturn(aliPayServiceImpl);

        //3、执行单元测试代码
        payServiceImpl.unifiedPay(unifiedPayDTO);

        //通过断言的方式进行验证，以确保测试结果符合预期
        //验证分布式锁获取方法执行过
        verify(redisLockRegistry).obtain(any(String.class));

        //验证数据库方法执行过
        verify(payOrderDao).selectByMap(any(Map.class));

        //验证支付账单入库逻辑执行过
        verify(payOrderDao).insert(any(PayOrderPO.class));

        //验证工厂方法执行过
        verify(payChannelServiceFactory).createPayChannelService(any(Integer.class));

        //验证支付方法执行过
        verify(aliPayServiceImpl).pay(any(UnifiedPayDTO.class));

    }
}
