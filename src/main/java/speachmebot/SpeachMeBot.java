package speachmebot;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.ZoneId.systemDefault;

public class SpeachMeBot {

    private static final int ONE_WEEK_MILLISECONDS = 1000 * 60 * 60 * 24 * 7;

    public static void main(String[] args) {
        SlackSession session = SlackSessionFactory.createWebSocketSlackSession(args[0]);

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

            scheduleAtFixedRate(DayOfWeek.FRIDAY, 15, 0, ONE_WEEK_MILLISECONDS, () ->
                    session.getChannels().stream()
                            .filter(channel -> "magicians".equals(channel.getName()))
                            .findFirst()
                            .ifPresent(magicians -> session.sendMessage(magicians, "trop tard pour vos CRA les gars ^^"))
            );

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

    private static void scheduleAtFixedRate(DayOfWeek dayOfWeek, int hour, int minutes, int period, Runnable task) {
        LocalDateTime now = LocalDateTime.now();
        int deltaDaysToFriday = (dayOfWeek.getValue() - now.getDayOfWeek().getValue()) % 7;
        LocalDateTime nextFriday = now.plusDays(deltaDaysToFriday).withHour(hour).withMinute(minutes).withSecond(0).withNano(0);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, Date.from(nextFriday.atZone(systemDefault()).toInstant()), period);
    }

}
