package speachmebot.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import speachmebot.domain.Message;
import speachmebot.domain.MessageRules;

public class EventsPosted implements SlackMessagePostedListener {

    private final MessageRules messageRules = new MessageRules();

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {
        Message message = new SlackMessagePostedToMessage(event, session);

        messageRules.accept(message);
    }

}
