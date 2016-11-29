package speachmebot.domain;

import java.util.Random;

public interface Message {
    String sender();

    String channel();

    boolean isDirect();

    void reply(String reply);

    boolean reply(String channelName, String message);

    Random random();

    boolean senderIsBot();

    String content();

    String myself();
}
