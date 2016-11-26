package speachmebot.domain.rule;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class CommandosAssignerTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private SlackSession session;

    @Mock
    private SlackChannel channel;

    @Test
    public void should_assign_next_commandos() {
        given(session.findChannelByName("commando")).willReturn(channel);
        given(channel.getTopic()).willReturn("commando : @loic ; sous-commando : @mcharmet\r\n");
        CommandosAssigner commandosAssigner = new CommandosAssigner();

        commandosAssigner.run(session);

        verify(session).setChannelTopic(channel, "commando : @frederic ; sous-commando : @loic");
        verify(session).sendMessageToUser("mcharmet", "salut mcharmet, j'ai le plaisir de t'annoncer que tu n'es plus sous-commando cette semaine ; reste quand même vigilant aux demandes :)", null);
        verify(session).sendMessageToUser("loic", "salut loic, tu étais commando la semaine dernière, tu deviens sous-commando cette semaine", null);
        verify(session).sendMessageToUser("frederic", "salut frederic, j'ai le regret de t'annoncer que tu es le commando cette semaine : active les notifications sur #commando et regarde tes mails - courage !", null);
        verify(session).sendMessage(channel, "J'ai mis à jour le sujet du channel, le nouveau commando c'est @frederic et @loic passe sous-commando. Bonne semaine à tous !");
    }

    @Test
    public void should_fail_to_assign_commandos_when_topic_is_not_what_is_expected() {
        given(session.findChannelByName("commando")).willReturn(channel);
        given(channel.getTopic()).willReturn("some other topic than expected one\r\n");
        CommandosAssigner commandosAssigner = new CommandosAssigner();

        commandosAssigner.run(session);

        verify(session).sendMessage(channel, "je suis perdu : le sujet du channel commando ne correspond pas à ce que je m'attendais à lire `commando : @(.+) ; sous-commando : @(.+)`");
    }
}