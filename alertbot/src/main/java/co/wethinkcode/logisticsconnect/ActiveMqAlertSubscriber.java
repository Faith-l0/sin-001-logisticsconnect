package co.wethinkcode.logisticsconnect;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Listens to the package-status topic and hands stage changes to the bot. */
public class ActiveMqAlertSubscriber {

    static final String CLIENT_ID = "alertbot";
    static final String SUBSCRIPTION_NAME = "alertbot-stage-updates";

    private static final Logger log = LoggerFactory.getLogger(ActiveMqAlertSubscriber.class);

    private final String brokerUrl;
    private final String topic;
    private final AlertBot bot;
    private volatile Connection connection;

    public ActiveMqAlertSubscriber(String brokerUrl, String topic, AlertBot bot) {
        this.brokerUrl = brokerUrl;
        this.topic = topic;
        this.bot = bot;
    }

    /** Connects on a background thread so the service still starts when the broker is down. */
    public void startInBackground() {
        Thread thread = new Thread(this::connect, "alert-subscriber");
        thread.setDaemon(true);
        thread.start();
    }

    private void connect() {
        try {
            // "failover:" keeps retrying and re-subscribes if the broker goes away and comes back.
            connection = new ActiveMQConnectionFactory("failover:(" + brokerUrl + ")").createConnection();
            connection.setClientID(CLIENT_ID);
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic stageTopic = session.createTopic(topic);
            // Durable, with its own client ID, so it does not clash with transit-service's subscription.
            MessageConsumer consumer = session.createDurableSubscriber(stageTopic, SUBSCRIPTION_NAME);
            consumer.setMessageListener(this::onMessage);
            connection.start();
            log.info("Subscribed to {}", topic);
        } catch (JMSException e) {
            log.error("Could not subscribe to {}: {}", topic, e.getMessage());
        }
    }

    private void onMessage(Message message) {
        if (message instanceof TextMessage text) {
            try {
                handleText(text.getText());
            } catch (JMSException e) {
                log.warn("Could not read message: {}", e.getMessage());
            }
        } else {
            log.warn("Ignoring a message that is not text");
        }
    }

    /** Package-private so it can be tested without a broker. A bad message is dropped, never fatal. */
    void handleText(String text) {
        try {
            bot.onUpdate(StageMessage.parse(text));
        } catch (IllegalArgumentException e) {
            log.warn("Ignoring bad stage message: {}", e.getMessage());
        }
    }
}
