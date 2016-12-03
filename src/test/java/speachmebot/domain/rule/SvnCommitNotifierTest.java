package speachmebot.domain.rule;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class SvnCommitNotifierTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private VersionningControlSystem versionningControlSystem;

    @Mock
    private SlackSession slackSession;

    @Mock
    private SlackChannel slackChannel;

    @Mock
    private SlackUser slackUser;

    @Captor
    private ArgumentCaptor<SlackAttachment> slackAttachment;

    @Test
    public void should_not_post_anything_if_there_is_no_new_commit() {
        given(versionningControlSystem.getLastCommitId()).willReturn("cb6d219");
        given(versionningControlSystem.getLastCommits("cb6d219")).willReturn(emptyList());
        SvnCommitNotifier svnCommitNotifier = new SvnCommitNotifier("", versionningControlSystem, null);

        svnCommitNotifier.run(slackSession);

        verifyNoMoreInteractions(slackSession);
    }

    @Test
    public void should_not_post_anything_if_channel_doesnt_exists() {
        given(versionningControlSystem.getLastCommitId()).willReturn("cb6d219");
        given(versionningControlSystem.getLastCommits("cb6d219")).willReturn(singletonList(new VersionningControlSystem.Commit(
                "",
                "",
                "",
                0
        )));
        given(slackSession.findChannelByName("sourcecode")).willReturn(null);
        SvnCommitNotifier svnCommitNotifier = new SvnCommitNotifier("", versionningControlSystem, null);

        svnCommitNotifier.run(slackSession);

        verify(slackSession).findChannelByName("sourcecode");
        verifyNoMoreInteractions(slackSession);
    }

    @Test
    public void should_post_a_commit_report_if_there_is_new_commit() {
        given(versionningControlSystem.getLastCommitId()).willReturn("cb6d219");
        given(versionningControlSystem.getLastCommits("cb6d219")).willReturn(singletonList(new VersionningControlSystem.Commit(
                "5bcdf67",
                "author",
                "message\n\nmore information",
                3
        )));
        given(slackSession.findChannelByName("sourcecode")).willReturn(slackChannel);
        SvnCommitNotifier svnCommitNotifier = new SvnCommitNotifier("http://vcs.url/commit", versionningControlSystem, emptyMap());

        svnCommitNotifier.run(slackSession);

        verify(slackSession).sendMessage(eq(slackChannel), eq(""), slackAttachment.capture());
        assertThat(slackAttachment.getValue())
                .extracting("authorName", "color", "title", "fallback", "titleLink", "text")
                .containsExactly("author", "#FC7A25", "#5bcdf67 message", "#5bcdf67", "http://vcs.url/commit-5bcdf67", "more information\n(3 fichiers touchés)");
    }

    @Test
    public void should_not_display_s_if_there_is_only_one_updated_file() {
        given(versionningControlSystem.getLastCommitId()).willReturn("cb6d219");
        given(versionningControlSystem.getLastCommits("cb6d219")).willReturn(singletonList(new VersionningControlSystem.Commit(
                "",
                "",
                "",
                1
        )));
        given(slackSession.findChannelByName("sourcecode")).willReturn(slackChannel);
        SvnCommitNotifier svnCommitNotifier = new SvnCommitNotifier("", versionningControlSystem, emptyMap());

        svnCommitNotifier.run(slackSession);

        verify(slackSession).sendMessage(eq(slackChannel), eq(""), slackAttachment.capture());
        assertThat(slackAttachment.getValue().getText()).isEqualTo("(1 fichier touché)");
    }

    @Test
    public void should_quote_author_if_mapping_is_found() {
        given(versionningControlSystem.getLastCommitId()).willReturn("cb6d219");
        given(versionningControlSystem.getLastCommits("cb6d219")).willReturn(singletonList(new VersionningControlSystem.Commit(
                "",
                "author",
                "",
                1
        )));
        given(slackSession.findChannelByName("sourcecode")).willReturn(slackChannel);
        given(slackSession.findUserByUserName("slackuser")).willReturn(slackUser);
        given(slackUser.getId()).willReturn("961accb");
        SvnCommitNotifier svnCommitNotifier = new SvnCommitNotifier("", versionningControlSystem, singletonMap("author", "slackuser"));

        svnCommitNotifier.run(slackSession);

        verify(slackSession).sendMessage(eq(slackChannel), eq(""), slackAttachment.capture());
        assertThat(slackAttachment.getValue().getAuthorName()).isEqualTo("<@961accb>");
    }

}
