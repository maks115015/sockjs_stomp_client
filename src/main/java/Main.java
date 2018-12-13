import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final String HOST =  "https://exrates.me";
    private static final String HEADER_HOST =  "www.exrates.me";
    private final static String SESSION_HEADER_NAME = "JSESSIONID";

    public static void main(String[] args) throws Exception {
        Main client = new Main();
        System.out.println("connect");
        ListenableFuture<StompSession> f = client.connect();
        StompSession stompSession = f.get();
        System.out.println("Subscribing to topic using session " + stompSession.getSessionId());
        new Scanner(System.in).nextLine();
    }

    private ListenableFuture<StompSession> connect() {
        /*step 1 get csrf token and jsessionid*/
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        requestHeaders.set("Host", HEADER_HOST);
        requestHeaders.set("Upgrade-Insecure-Requests", "1");
        requestHeaders.set("Connection", "keep-alive");
        requestHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
        HttpEntity entity = new HttpEntity(requestHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                HOST.concat("/adsffefe/csrf"), HttpMethod.GET, entity, String.class, "");
        HttpHeaders responseHeaders = response.getHeaders();
        /*get header SET COOKIE and get JSESSIONID from it*/
        List<String> setCookie = Stream.of(responseHeaders.get(HttpHeaders.SET_COOKIE).stream().filter(p->p.startsWith(SESSION_HEADER_NAME)).findFirst().get().split(";")).collect(Collectors.toList());
        String sessionId = setCookie.stream().map(p->p.split("=")).collect(Collectors.toMap(a -> a[0], a -> a.length>1? a[1]: "")).get(SESSION_HEADER_NAME);
        /*------------------------*/
        String csrfToken = new JSONObject(response.getBody()).getString("token");
        System.out.println("csrf token " + csrfToken);
        System.out.println(SESSION_HEADER_NAME + " " + sessionId);
        /*------------------------*/
        /*step 2 create websocket connection with sockjs and stomp, create request hqeders*/
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.set("Host", HEADER_HOST);
        headers.set("Upgrade-Insecure-Requests", "1");
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
        handshakeHeaders.set("Cookie", "JSESSIONID=".concat(sessionId));
        handshakeHeaders.putAll(headers);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("X-CSRF-TOKEN", csrfToken);
        /*----------------*/
        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);
        /*------------*/
        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new StringMessageConverter());
        /*step 3 - connect to socket, subscribe and
        * subscribe to endpoint and handle responses in MyStompSessionHandler.class */
        return stompClient.connect(HOST.concat("/public_socket/"), handshakeHeaders, connectHeaders, new MyStompSessionHandler());
    }

}
