package co.wethinkcode.logisticsconnect;

import co.wethinkcode.logisticsconnect.mq.MqConfig;
import io.javalin.Javalin;

public class AlertBotApp {

    public static void main(String[] args) {
        SimulatedSocialPoster poster = new SimulatedSocialPoster();
        new ActiveMqAlertSubscriber(MqConfig.BROKER_URL, MqConfig.TOPIC, new AlertBot(poster))
                .startInBackground();
        createApp(poster).start(7054);
    }

    /** Builds the app without starting it, so tests can pick their own port. */
    static Javalin createApp(SimulatedSocialPoster poster) {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        // Everything the bot has "posted" so far, oldest first.
        app.get("/alerts", ctx -> ctx.json(poster.posts()));

        return app;
    }
}
