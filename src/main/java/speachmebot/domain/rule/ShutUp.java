package speachmebot.domain.rule;

import speachmebot.domain.Message;

import java.util.Optional;
import java.util.function.Function;

public class ShutUp implements Function<Message, Optional<Message>> {
    public Optional<Message> apply(Message message) {
        if (("marion".equals(message.sender())
                || "rose".equals(message.sender())
                || "rosette".equals(message.sender()))
                && "bullshit".equals(message.channel())
                && message.random().nextInt(3) == 0) {
            message.reply("TG");
            return Optional.empty();
        }

        return Optional.of(message);
    }
}
