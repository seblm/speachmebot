package speachmebot;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

public class EventsPosted implements SlackMessagePostedListener {

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {

        //Si c'est un bot on ne fait rien
        if (event.getSender().isBot()) {
            return;
        }

        //Gestion event sur Marion et Rose
        if ((event.getSender().getUserName().equals("marion") || event.getSender().getUserName().equals("rose")) && event.getChannel().getName().equals("bullshit")) {
            int arrondi = Double.valueOf(Math.floor(Math.random() * 3)).intValue();
            boolean activeBot = arrondi == 0;

            if (activeBot) {
                session.sendMessage(event.getChannel(), "TG");
                return;
            }
        }

        //Réaction générale sur citation
        if (event.getMessageContent().contains("<@" + session.sessionPersona().getId() + ">")) {
            session.sendMessage(event.getChannel(), "hodor");
        }

    }

}
