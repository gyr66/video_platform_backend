package com.gyr.minio.controller;

import com.gyr.minio.service.VideoProcessingService;
import com.gyr.minio.utils.VideoFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;


@ServerEndpoint("/websocket")
@Component
public class WebSocketController {

    Logger logger = LoggerFactory.getLogger(getClass());

    Session session;

    private static VideoFetcher videoFetcher;

    private static VideoProcessingService processor;

    @Autowired
    public void setVideoFetcher(VideoFetcher videoFetcher, VideoProcessingService videoProcessingService){
        WebSocketController.videoFetcher = videoFetcher;
        WebSocketController.processor = videoProcessingService;
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        sendMessage("连接服务器成功!");
    }

    @OnClose
    public void onClose() {
        logger.info("断开了一个websocket的连接");
    }


    @OnMessage
    public void onMessage(String url) throws IOException, InterruptedException {
        Map<String, Object> result = videoFetcher.fetch(url);
        System.out.println(result);
        Process process = (Process) result.get("process");
        if (process == null) {
            sendMessage("获取失败远程资源失败!");
            return;
        }
        Scanner scanner = new Scanner(process.getInputStream());
        while (scanner.hasNextLine()) sendMessage(scanner.nextLine());
        process.waitFor();
        int res = process.exitValue();
        if (res == 0) {
            sendMessage("获取远程资源成功!");
            sendMessage("done");
            WebSocketController.processor.process((String) result.get("path"), "网络上传", 0);
        }
        else sendMessage("获取远程资源失败!");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.error(error.getMessage());
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }
}
