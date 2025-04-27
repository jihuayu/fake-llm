package com.jihuayu.fakellm;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1")
public class GatewayController {

    private final TaskExecutor taskExecutor;

    public GatewayController(@Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @PostMapping("/chat/completions")
    public Object send(@RequestBody JSONObject params) {
        if (params.containsKey("stream") && params.getBoolean("stream")) {
            params.put("stream", false);
            return this.streamOpenAIResponse(params);
        }
        String response = Faker.getResponse(params.getString("model"));

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(response);
    }


    public Object streamOpenAIResponse(@RequestBody JSONObject params) {
        SseEmitter emitter = new SseEmitter(180000L);
        if (params.containsKey("stream") && params.getBoolean("stream")) {
            params.put("stream", false);
        }

        taskExecutor.execute(() -> {
            try {

                String completeResponse = Faker.getResponse(params.getString("model"));
                // 解析JSON响应
                JSONObject responseJson = JSONObject.parseObject(completeResponse);

                responseJson.put("object", "chat.completion.chunk");
                JSONObject choices = responseJson.getJSONArray("choices").getJSONObject(0);
                choices.put("delta", choices.get("message"));
                choices.remove("message");

                emitter.send(SseEmitter.event().data(" " + responseJson, MediaType.APPLICATION_JSON));
                // 发送流结束信号
                emitter.send(SseEmitter.event().data(" [DONE]", MediaType.APPLICATION_JSON));
                // 完成请求
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }


}
