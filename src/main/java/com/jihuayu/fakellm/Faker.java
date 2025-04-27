package com.jihuayu.fakellm;

import com.alibaba.fastjson2.JSONObject;

import java.util.Date;
import java.util.Random;

public class Faker {

    public static final String JSON = """
            {
              "id": "chatcmpl-680d918214211bce71b1bed1",
              "object": "chat.completion",
              "created": 1745719683,
              "model": "moonshot-v1-8k",
              "choices": [
                {
                  "index": 0,
                  "message": {
                    "role": "assistant",
                    "content": "这是一个虚假的测试LLM，它总是返回这个内容"
                  },
                  "finish_reason": "stop"
                }
              ],
              "usage": {
                "prompt_tokens": 9,
                "completion_tokens": 49,
                "total_tokens": 58
              }
            }
            """;

    public static final JSONObject jsonObject = JSONObject.parseObject(JSON);

    public static String getResponse(String model) {
        jsonObject.put("model", model);
        jsonObject.put("created", new Date().getTime());
        jsonObject.put("id", generateRandomChatId());
        return jsonObject.toString();
    }

    public static String generateRandomChatId() {
        // 前缀
        String prefix = "chatcmpl-";

        // 生成随机字符串部分 (24个字符)
        StringBuilder randomPart = new StringBuilder();
        String allowedChars = "0123456789abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();

        for (int i = 0; i < 24; i++) {
            int index = random.nextInt(allowedChars.length());
            randomPart.append(allowedChars.charAt(index));
        }

        // 组合前缀和随机部分
        return prefix + randomPart;
    }
}
