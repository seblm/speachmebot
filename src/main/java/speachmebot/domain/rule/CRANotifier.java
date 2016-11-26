package speachmebot.domain.rule;

import com.ullink.slack.simpleslackapi.SlackSession;
import speachmebot.ScheduledTask;

import java.util.Optional;

public class CRANotifier implements ScheduledTask {

    @Override
    public String name() {
        return "CRA notifier";
    }

    @Override
    public void run(SlackSession session) {
        Optional.ofNullable(session.findChannelByName("magicians")).ifPresent(magicians ->
                session.sendMessage(magicians, "trop tard pour vos CRA les gars ^^"));
    }

}
