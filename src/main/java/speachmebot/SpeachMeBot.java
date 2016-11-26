package speachmebot;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import speachmebot.slack.EventsPosted;

import java.io.IOException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.ZoneId.systemDefault;

public class SpeachMeBot {

    private static final int ONE_WEEK_MILLISECONDS = 1000 * 60 * 60 * 24 * 7;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeachMeBot.class);

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

            // TODO scheduleAtFixedRate should be refactored to use rules or something

            scheduleAtFixedRate(DayOfWeek.FRIDAY, 15, 0, ONE_WEEK_MILLISECONDS, "CRA Notifier", () ->
                    Optional.ofNullable(session.findChannelByName("magicians")).ifPresent(magicians ->
                            session.sendMessage(magicians, "trop tard pour vos CRA les gars ^^"))
            );

            scheduleAtFixedRate(DayOfWeek.MONDAY, 9, 0, ONE_WEEK_MILLISECONDS, "commando assigner", () -> {
                Optional.ofNullable(session.findChannelByName("commando")).ifPresent(commandoChannel -> {

                    Map<String, String> commandosBySousCommandos = new HashMap<>();
                    commandosBySousCommandos.put("sylvain", "sebastian");
                    commandosBySousCommandos.put("sebastian", "florian");
                    commandosBySousCommandos.put("florian", "mcharmet");
                    commandosBySousCommandos.put("mcharmet", "loic");
                    commandosBySousCommandos.put("loic", "frederic");
                    commandosBySousCommandos.put("frederic", "alexis");

                    String regex = "commando : @(.+) ; sous-commando : @(.+)";
                    Matcher matcher = Pattern.compile(regex).matcher(commandoChannel.getTopic().replaceAll("\r\n", ""));
                    if (matcher.matches()) {
                        String ancienSousCommando = matcher.group(2);
                        String nouveauSousCommando = matcher.group(1);
                        String nouveauCommando = commandosBySousCommandos.get(nouveauSousCommando);
                        session.setChannelTopic(commandoChannel, "commando : @" + nouveauCommando + " ; sous-commando : @" + nouveauSousCommando);
                        session.sendMessageToUser(ancienSousCommando, "salut " + ancienSousCommando + ", j'ai le plaisir de t'annoncer que tu n'es plus sous-commando cette semaine ; reste quand même vigilant aux demandes :)", null);
                        session.sendMessageToUser(nouveauSousCommando, "salut " + nouveauSousCommando + ", tu étais commando la semaine dernière, tu deviens sous-commando cette semaine", null);
                        session.sendMessageToUser(nouveauCommando, "salut " + nouveauCommando + ", j'ai le regret de t'annoncer que tu es le commando cette semaine : active les notifications sur #commando et regarde tes mails - courage !", null);
                        session.sendMessage(commandoChannel, "@channel j'ai mis à jour le sujet du channel, le nouveau commando c'est @" + nouveauCommando + " et @" + nouveauSousCommando + " passe sous-commando. Bonne semaine à tous !");
                    } else {
                        session.sendMessage(commandoChannel, "je suis perdu : le sujet du channel commando ne correspond pas à ce que je m'attendais à lire \"`" + regex + "`\"");
                    }
                });
            });

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

    private static void scheduleAtFixedRate(DayOfWeek dayOfWeek, int hour, int minutes, int period, String taskName, Runnable task) {
        LocalDateTime nextFriday = nextDayOfWeek(Clock.systemDefaultZone(), dayOfWeek).atStartOfDay().withHour(hour).withMinute(minutes);
        LOGGER.info("waiting until {} to run {} and then every {}ms", nextFriday, taskName, period);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, Date.from(nextFriday.atZone(systemDefault()).toInstant()), period);
    }

    static LocalDate nextDayOfWeek(Clock clock, DayOfWeek dayOfWeek) {
        LocalDate now = LocalDate.now(clock);
        int deltaDays = dayOfWeek.getValue() - now.getDayOfWeek().getValue();
        if (deltaDays < 0) {
            deltaDays += 7;
        }
        return now.plusDays(deltaDays);
    }

}
