package speachmebot.domain.rule;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import speachmebot.domain.Message;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ParrotTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private Message message;

    @Test
    public void should_send_message_to_a_channel() {
        given(message.isDirect()).willReturn(true);
        given(message.content()).willReturn("dis sur <#C1503MPDL|general> salut @pseudo comment ça va ?");
        given(message.reply("general", "salut @pseudo comment ça va ?")).willReturn(true);
        Parrot parrot = new Parrot();

        Optional<Message> maybeMessage = parrot.apply(message);

        assertThat(maybeMessage).isEmpty();
        verify(message).reply("general", "salut @pseudo comment ça va ?");
        verify(message, never()).reply(anyString());
    }

    @Test
    public void should_not_send_message_to_an_unknown_channel() {
        given(message.isDirect()).willReturn(true);
        given(message.content()).willReturn("dis sur <#C1503MPDL|unknown> salut @pseudo comment ça va ?");
        Parrot parrot = new Parrot();

        Optional<Message> maybeMessage = parrot.apply(message);

        assertThat(maybeMessage).isEmpty();
        verify(message).reply("désolé mais l'envoi du message sur #unknown a échoué");
    }

    @Test
    public void should_not_send_message_if_message_is_not_direct() {
        given(message.isDirect()).willReturn(false);
        Parrot parrot = new Parrot();

        Optional<Message> maybeMessage = parrot.apply(message);

        assertThat(maybeMessage).isNotEmpty();
    }

}
