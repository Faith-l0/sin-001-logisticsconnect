package co.wethinkcode.logisticsconnect;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

/** Publishes stage changes to an ActiveMQ topic. Opens a short-lived connection per message. */
public class ActiveMqStagePublisher implements StageChangePublisher {

    private final String brokerUrl;
    private final String topic;

    public ActiveMqStagePublisher(String brokerUrl, String topic) {
        this.brokerUrl = brokerUrl;
        this.topic = topic;
    }

    @Override
    public void publish(String hubId, int stage) {
        Connection connection = null;
        try {
            connection = new ActiveMQConnectionFactory(brokerUrl).createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createTopic(topic));
            producer.send(session.createTextMessage(StageMessage.toJson(hubId, stage)));
        } catch (JMSException e) {
            throw new PublishFailedException("could not publish to " + topic + ": " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ignored) {
                    // nothing useful to do if closing fails
                }
            }
        }
    }
}
