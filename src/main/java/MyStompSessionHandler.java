
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;


public class MyStompSessionHandler extends StompSessionHandlerAdapter {

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("try to subscribe ");
        String destination = "/app/orders/sfwfrf442fewdf/4";
        session.subscribe(destination, this);
        System.out.println("subscribed");
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("Received : " + getPayloadType(headers));
        System.out.println(payload);
        System.out.println(headers);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        System.out.println("exception " + exception.getMessage());
        exception.printStackTrace();
        super.handleException(session, command, headers, payload, exception);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        System.out.println("transport exception " + exception);
        super.handleTransportError(session, exception);
    }
}
