package com.wei.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class CommodityService {

    LinkedBlockingQueue<Request> queue = new LinkedBlockingQueue<>();

    @Autowired
    private QueryServiceRemoteCall queryServiceRemoteCall;

    /**
     * 模拟请求体（内部类）
     */
    class Request{
        String code;
        CompletableFuture<Map<String, Object>> future;
    }

    /**
     * 定时任务，每隔10ms发送一次请求
     */
    @PostConstruct
    public void init() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(() -> {
            int size = queue.size();
            if (size ==0) {
                return;
            }
            ArrayList<Request> requests = new ArrayList<>();
            for (int i = 0; i < size; i ++) {
                requests.add(queue.poll());
            }
            System.out.println("合并了" + size + "个请求");
            List<String> codes = new ArrayList<>();
            for (Request request : requests) {
                codes.add(request.code);
            }
            List<Map<String,Object>> responses = queryServiceRemoteCall.queryCommodityByCodeBatch(codes);
            Map<String, Map<String, Object>> responseMap = new HashMap<>();
            for (Map<String, Object> response : responses) {
                String code = response.get("code").toString();
                responseMap.put(code, response);
            }
            for (Request request : requests) {
                Map<String, Object> res = responseMap.get(request.code);
                request.future.complete(res);
            }
        },0,10, TimeUnit.MILLISECONDS);
    }

    /**
     * 请求下发
     * @param code
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Map<String,Object> queryCommodity(String code) throws ExecutionException, InterruptedException {
        Request request = new Request();
        request.code = code;
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        queue.add(request);
        return future.get();
      //  return queryServiceRemoteCall.queryCommodityByCode(code);
    }
}
