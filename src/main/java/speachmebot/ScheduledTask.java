package speachmebot;

import com.ullink.slack.simpleslackapi.SlackSession;

public interface ScheduledTask {

    String name();

    void run(SlackSession session);

}
