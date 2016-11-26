package speachmebot.domain.rule;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CRANotifierTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private SlackSession session;

    @Mock
    private SlackChannel channel;

    @Test
    public void should_notifies_CRA() {
        given(session.findChannelByName("magicians")).willReturn(channel);

        new CRANotifier().run(session);

        verify(session).sendMessage(channel, "trop tard pour vos CRA les gars ^^");
    }

    @Test
    public void should_not_notifies_CRA_if_channel_is_not_found() {
        given(session.findChannelByName("magicians")).willReturn(null);

        new CRANotifier().run(session);

        verify(session, never()).sendMessage(any(SlackChannel.class), anyString());
    }

}
