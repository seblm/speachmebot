package speachmebot.domain;

import java.util.Random;

public interface Message {
    String sender();

    String channel();

    void reply(String reply);

    Random random();

    boolean senderIsBot();

    String content();

    String myself();
}
