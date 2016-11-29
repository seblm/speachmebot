package speachmebot.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import speachmebot.domain.Message;

import java.util.Optional;
import java.util.Random;

public class SlackMessagePostedToMessage implements Message {

    private final SlackMessagePosted event;
    private final SlackSession session;

    SlackMessagePostedToMessage(SlackMessagePosted event, SlackSession session) {
        this.event = event;
        this.session = session;
    }

    @Override
    public String sender() {
        return event.getSender().getUserName();
    }

    @Override
    public String channel() {
        return event.getChannel().getName();
    }

    @Override
    public boolean isDirect() {
        return event.getChannel().isDirect();
    }

    @Override
    public void reply(String reply) {
        session.sendMessage(event.getChannel(), reply);
    }

    @Override
    public boolean reply(String channelName, String reply) {
        return Optional.ofNullable(session.findChannelByName(channelName))
                .map(channel -> {
                    session.sendMessage(channel, reply);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Random random() {
        return new Random();
    }

    @Override
    public boolean senderIsBot() {
        return event.getSender().isBot();
    }

    @Override
    public String content() {
        return event.getMessageContent();
    }

    @Override
    public String myself() {
        return "<@" + session.sessionPersona().getId() + ">";
    }
}
