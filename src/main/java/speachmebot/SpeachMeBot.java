package speachmebot;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import speachmebot.domain.rule.CRANotifier;
import speachmebot.domain.rule.CommandosAssigner;
import speachmebot.slack.EventsPosted;

import java.io.IOException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.ZoneId.systemDefault;

public class SpeachMeBot {

    private static final int ONE_WEEK_MILLISECONDS = 1000 * 60 * 60 * 24 * 7;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeachMeBot.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("need API Token as unique program argument");
            System.exit(1);
        }

        new SpeachMeBot().connect(args[0]);
    }

    private final Timer timer;

    private SlackSession session;

    SpeachMeBot() {
        this.timer = new Timer();
    }

    void connect(String APIToken) {
        session = SlackSessionFactory.createWebSocketSlackSession(APIToken);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                session.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        try {
            session.connect();

            session.addMessagePostedListener(new EventsPosted());

            scheduleAtFixedRate(DayOfWeek.FRIDAY, 15, 0, ONE_WEEK_MILLISECONDS, new CRANotifier());
            scheduleAtFixedRate(DayOfWeek.MONDAY, 9, 0, ONE_WEEK_MILLISECONDS, new CommandosAssigner());

            while (true) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                session.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void scheduleAtFixedRate(DayOfWeek dayOfWeek, int hour, int minutes, int period, ScheduledTask task) {
        LocalDateTime nextFriday = nextDayOfWeek(Clock.systemDefaultZone(), dayOfWeek).atStartOfDay().withHour(hour).withMinute(minutes);
        LOGGER.info("waiting until {} to run {} and then every {}ms", nextFriday, task.name(), period);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run(session);
            }
        }, Date.from(nextFriday.atZone(systemDefault()).toInstant()), period);
    }

    LocalDate nextDayOfWeek(Clock clock, DayOfWeek dayOfWeek) {
        LocalDate now = LocalDate.now(clock);
        int deltaDays = dayOfWeek.getValue() - now.getDayOfWeek().getValue();
        if (deltaDays < 0) {
            deltaDays += 7;
        }
        return now.plusDays(deltaDays);
    }

}
