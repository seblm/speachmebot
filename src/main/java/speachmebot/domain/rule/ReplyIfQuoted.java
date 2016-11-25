package speachmebot.domain.rule;

import speachmebot.domain.Message;

import java.util.Optional;
import java.util.function.Function;

public class ReplyIfQuoted implements Function<Message, Optional<Message>> {

    @Override
    public Optional<Message> apply(Message message) {
        if (message.content().contains(message.myself())) {
            message.reply("hodor");
            return Optional.empty();
        }

        return Optional.of(message);
    }

}
