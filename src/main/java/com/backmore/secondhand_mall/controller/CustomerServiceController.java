package com.backmore.secondhand_mall.controller;

import com.alibaba.fastjson2.JSONObject;
import com.backmore.secondhand_mall.entity.MessageRequest;
import com.backmore.secondhand_mall.vo.ResponseV0;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/customerService")
public class CustomerServiceController {

    @Resource
    private OllamaChatModel ollamaChatModel;
    @Resource
    private RestTemplate restTemplate;
    
    @Autowired
    private Environment env;

    @PostMapping("/message")
    public ResponseV0<MessageRequest> receiveMessage(@RequestBody MessageRequest message) {
//        获取sessionId
        String sessionId = message.getSessionId();
//        获取用户信息
        String message1 = message.getMessage();
        String  messages = "请使用中文简体回答：" + message.getMessage();
        Prompt prompt = new Prompt(new UserMessage(messages));
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        String content = chatResponse.getResult().getOutput().getText();
        content = content.split("</think>")[1];
        System.out.println("content = " + content);
        MessageRequest responseMessage = new MessageRequest();
        responseMessage.setMessage(content);
        return ResponseV0.success(responseMessage);
    }


    @PostMapping("/chat")
    public ResponseV0<MessageRequest> receiveMessage2 (@RequestBody MessageRequest message) {
//       请求体数据
        Map<String, Object> requestBody = new HashMap<>();
        String message1 = message.getMessage();
        requestBody.put("message",  message1);
        requestBody.put("mode", "chat");
        requestBody.put("userId", 1);
//  请求头数据
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization","Bearer HBZ91ZY-XB5MYRP-MB8F78H-9FBECFH");
        requestHeaders.set("accept", "application/json");
        HttpEntity<Map<String, Object>> r = new HttpEntity<Map<String, Object>>(requestBody, requestHeaders);

        String url = env.getProperty("ai.service.url");
        String content = restTemplate.postForObject(url, r, String.class);

        JSONObject jsonObject = JSONObject.parseObject(content);
        String aiAnswer = (String)jsonObject.get("textResponse");
        content = aiAnswer.split("</think>")[1];
        MessageRequest responseMessage2 = new MessageRequest();
        responseMessage2.setMessage(content);
        return ResponseV0.success(responseMessage2);
    }

}