package speachmebot;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import speachmebot.domain.rule.CRANotifier;
import speachmebot.domain.rule.CommandosAssigner;
import speachmebot.domain.rule.SvnCommitNotifier;
import speachmebot.slack.EventsPosted;
import speachmebot.svn.Svn;

import java.io.IOException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.ZoneId.systemDefault;

public class SpeachMeBot {

    private static final long ONE_MINUTE_MILLISECONDS = 1000 * 60;
    private static final long ONE_WEEK_MILLISECONDS = ONE_MINUTE_MILLISECONDS * 60 * 24 * 7;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeachMeBot.class);

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("usage: <API Token> <svnUrl> <svnUserName> <svnPassword> <sourceViewerUrl>");
            System.exit(1);
        }

        new SpeachMeBot().connect(args[0], args[1], args[2], args[3], args[4]);
    }

    private final Timer timer;

    private SlackSession session;

    SpeachMeBot() {
        this.timer = new Timer();
    }

    private void connect(String APIToken, String svnUrl, String svnUserName, String svnPassword, String sourceViewerUrl) {
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
            Map<String, String> pseudoBySvnUserName = new HashMap<>();
            pseudoBySvnUserName.put("achappron", "alexis");
            pseudoBySvnUserName.put("ccharmet", "mcharmet");
            pseudoBySvnUserName.put("fdurieux", "florian");
            pseudoBySvnUserName.put("fgloppe", "frederic");
            pseudoBySvnUserName.put("jrouet", "jeremy");
            pseudoBySvnUserName.put("lleroux", "loic");
            pseudoBySvnUserName.put("slemerdy", "sebastian");
            pseudoBySvnUserName.put("snoulet", "sylvain");

            SvnCommitNotifier svnCommitNotifier = new SvnCommitNotifier(sourceViewerUrl, new Svn(svnUserName, svnPassword, svnUrl), pseudoBySvnUserName);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    svnCommitNotifier.run(session);
                }
            }, 0L, ONE_MINUTE_MILLISECONDS);

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

    private void scheduleAtFixedRate(DayOfWeek dayOfWeek, int hour, int minutes, long period, ScheduledTask task) {
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
