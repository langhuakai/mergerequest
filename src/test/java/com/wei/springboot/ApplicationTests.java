package com.wei.springboot;

import com.wei.springboot.service.CommodityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
class ApplicationTests {

    public static final int THREAD_NUM = 200;
    CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);

    @Autowired
    private CommodityService commodityService;


    /**
     * 模拟并发请求
     * @throws IOException
     */
    @Test
    public void benchMark() throws IOException{
        for (int i = 0; i<THREAD_NUM; i++) {
            final String code = "code-" + (i + 1);
            Thread thread = new Thread(() -> {
                try {
                    countDownLatch.await();
                    Map<String,Object> result;
                    result = commodityService.queryCommodity(code);
                    System.out.println(Thread.currentThread().getName() + "查询结束，查询结果为" + result);
                } catch (Exception e) {
                    System.out.println(Thread.currentThread().getName() + "查询失败" + e.getMessage());
                    e.printStackTrace();
                }
            });
            thread.setName("price-thread-" + code);
            thread.start();
            countDownLatch.countDown();
        }
        System.in.read();

    }

    @Test
    void contextLoads() {
    }

}
