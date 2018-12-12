import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Main {

    private static final String HOST = /*"https://exrates.me";*/ "http://localhost:8080";
    private static final String HEADER_HOST = /*"exrates.me";*/ "localhost:8080";

    public static void main(String[] args) throws Exception {
        Main client = new Main();
        System.out.println("connect");
        ListenableFuture<StompSession> f = client.connect();
        StompSession stompSession = f.get();
        System.out.println("Subscribing to topic using session " + stompSession);
      /*  client.subscribeOrders(stompSession);*/

        new Scanner(System.in).nextLine();
    }

    private ListenableFuture<StompSession> connect() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.set("Host", HEADER_HOST);
        headers.set("Upgrade-Insecure-Requests", "1");
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");

        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                HOST.concat("/adsffefe/csrf"), HttpMethod.GET, entity, String.class, "");
        String csrfToken = new JSONObject(response.getBody()).getString("token");
        System.out.println("csrf token " + csrfToken);
        /*------------------------*/
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("X-CSRF-TOKEN", csrfToken);
        handshakeHeaders.set("Accept", "*/*");
        handshakeHeaders.set("Connection", "keep-alive");
        handshakeHeaders.set("Origin", "null");
        handshakeHeaders.set("Accept-Encoding", "gzip, deflate, br");
        handshakeHeaders.set("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
        handshakeHeaders.set("Host", HEADER_HOST);
        handshakeHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
        handshakeHeaders.putAll(headers);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.putAll(handshakeHeaders);
        /*----------------*/
        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);
        /*------------*/
        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new StringMessageConverter());

        return stompClient.connect("ws://localhost:8080/public_socket/", handshakeHeaders, connectHeaders, new MyStompSessionHandler());
    }

    private void subscribeOrders(StompSession stompSession) {
        stompSession.subscribe("/app/orders/sfwfrf442fewdf/4", new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                System.out.println("Received greeting " + new String((byte[]) o));
            }
        });
    }

    private class MyHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            System.out.println("Now connected");
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println(headers);
            super.handleFrame(headers, payload);
        }
    }

}
