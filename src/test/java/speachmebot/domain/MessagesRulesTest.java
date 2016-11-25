package speachmebot.domain;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.stubbing.answers.ThrowsException;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willReturn;

public class MessagesRulesTest {

    @Test
    public void should_does_nothing_when_sender_is_bot() {
        Message message = mock(Message.class, new ThrowsException(new AssertionError("should not invoke this method")));
        willReturn(true).given(message).senderIsBot();

        new MessageRules().accept(message);
    }

    @Test
    public void should_shut_up_if_marion_on_bullshit() {
        Message message = mock(Message.class, new ThrowsException(new AssertionError("should not invoke this method")));
        willReturn(false).given(message).senderIsBot();
        willReturn("marion").given(message).sender();
        willReturn("bullshit").given(message).channel();
        willReturn(mock(Random.class)).given(message).random();
        willDoNothing().given(message).reply(anyString());

        new MessageRules().accept(message);

        ArgumentCaptor<String> reply = ArgumentCaptor.forClass(String.class);
        verify(message).reply(reply.capture());
        assertThat(reply.getValue()).isEqualTo("TG");
    }

    @Test
    public void should_reply_to_quote() {
        Message message = mock(Message.class, new ThrowsException(new AssertionError("should not invoke this method")));
        willReturn(false).given(message).senderIsBot();
        willReturn(null).given(message).sender();
        willReturn("some string for <@you> bot").given(message).content();
        willReturn("<@you>").given(message).myself();
        willDoNothing().given(message).reply(anyString());

        new MessageRules().accept(message);

        ArgumentCaptor<String> reply = ArgumentCaptor.forClass(String.class);
        verify(message).reply(reply.capture());
        assertThat(reply.getValue()).isEqualTo("hodor");
    }

}
