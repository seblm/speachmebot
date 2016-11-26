package speachmebot.domain.rule;

import com.ullink.slack.simpleslackapi.SlackSession;
import speachmebot.ScheduledTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandosAssigner implements ScheduledTask {

    @Override
    public String name() {
        return "commandos assigner";
    }

    @Override
    public void run(SlackSession session) {
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
                session.sendMessage(commandoChannel, "J'ai mis à jour le sujet du channel, le nouveau commando c'est @" + nouveauCommando + " et @" + nouveauSousCommando + " passe sous-commando. Bonne semaine à tous !");
            } else {
                session.sendMessage(commandoChannel, "je suis perdu : le sujet du channel commando ne correspond pas à ce que je m'attendais à lire `" + regex + "`");
            }
        });
    }

}
