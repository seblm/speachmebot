package speachmebot.domain.rule;

import speachmebot.domain.Message;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

public class DoesNothingIfBot implements Function<Message, Optional<Message>> {
    @Override
    public Optional<Message> apply(Message message) {
        return ofNullable(message.senderIsBot() ? null : message);
    }
}
