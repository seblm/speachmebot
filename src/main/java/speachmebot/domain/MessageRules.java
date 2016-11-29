package speachmebot.domain;

import speachmebot.domain.rule.DoesNothingIfBot;
import speachmebot.domain.rule.Parrot;
import speachmebot.domain.rule.ReplyIfQuoted;
import speachmebot.domain.rule.ShutUp;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class MessageRules implements Consumer<Message> {

    private final List<Function<Message, Optional<Message>>> rules;

    public MessageRules() {
        rules = asList(
                new DoesNothingIfBot(),
                new Parrot(),
                new ShutUp(),
                new ReplyIfQuoted()
        );
    }

    @Override
    public void accept(Message message) {
        apply(message, rules);
    }

    private void apply(Message message, List<Function<Message, Optional<Message>>> rules) {
        if (rules.isEmpty()) {
            return;
        }
        rules.get(0).apply(message).ifPresent(m -> apply(message, rules.subList(1, rules.size())));
    }

}
