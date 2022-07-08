package com.nettyboot.websocket.test;

import com.nettyboot.util.StringUtil;
import com.nettyboot.websocket.client.WebSocketClient;
import com.nettyboot.websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Properties;

public class Tester {
    private static final Logger logger = LoggerFactory.getLogger(Tester.class);

    public static void main(String[] args) throws Exception {

        (new Thread(new Runnable() {
            @Override
            public void run() {
                Properties properties = new Properties();

                WebSocketServer wsServer = new WebSocketServer(properties);
                wsServer.start();
            }
        })).start();


        WebSocketClient client = new WebSocketClient("0.0.0.0", 8080);
        client.start();


        while(true) {
            String msg = StringUtil.randomString(StringUtil.randomInt(10, 30), false);
            client.sendMessage(msg);
            int waitTime =  StringUtil.randomInt(2, 40);
            logger.info("等待时间："+waitTime+"s");
            waitTime =  waitTime*1000;
            Thread.sleep(waitTime);
        }

    }
}
