package speachmebot.domain.rule;

import speachmebot.domain.Message;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parrot implements Function<Message, Optional<Message>> {

    private final Pattern pattern = Pattern.compile("dis sur <#C.+\\|(.+)> (.+)");

    @Override
    public Optional<Message> apply(Message message) {
        if (message.isDirect()) {
            Matcher matcher = pattern.matcher(message.content());
            if (matcher.matches()) {
                boolean success = message.reply(matcher.group(1), matcher.group(2));
                if (!success) {
                    message.reply("désolé mais l'envoi du message sur #" + matcher.group(1) + " a échoué");
                }
                return Optional.empty();
            }
        }
        return Optional.of(message);
    }

}
