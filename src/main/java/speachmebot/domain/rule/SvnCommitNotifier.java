package speachmebot.domain.rule;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import speachmebot.ScheduledTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.joining;

public class SvnCommitNotifier implements ScheduledTask {

    private final Map<String, String> pseudoBySvnUserName;
    private final String sourceViewerUrl;
    private final VersionningControlSystem versionningControlSystem;

    private String commitId;

    public SvnCommitNotifier(String sourceViewerUrl, VersionningControlSystem versionningControlSystem) {
        this.versionningControlSystem = versionningControlSystem;
        this.sourceViewerUrl = sourceViewerUrl;
        this.pseudoBySvnUserName = new HashMap<>();
        this.pseudoBySvnUserName.put("achappron", "alexis");
        this.pseudoBySvnUserName.put("ccharmet", "mcharmet");
        this.pseudoBySvnUserName.put("fdurieux", "florian");
        this.pseudoBySvnUserName.put("fgloppe", "frederic");
        this.pseudoBySvnUserName.put("jrouet", "jeremy");
        this.pseudoBySvnUserName.put("lleroux", "loic");
        this.pseudoBySvnUserName.put("slemerdy", "sebastian");
        this.pseudoBySvnUserName.put("snoulet", "sylvain");

        this.commitId = versionningControlSystem.getLastCommitId();
    }

    @Override
    public String name() {
        return "SvnCommitNotifier";
    }

    @Override
    public void run(SlackSession session) {
        List<VersionningControlSystem.Commit> lastCommits = versionningControlSystem.getLastCommits(this.commitId);
        lastCommits.forEach(commit -> Optional.ofNullable(session.findChannelByName("sourcecode")).ifPresent(sourcecode -> {
            SlackAttachment slackAttachment = new SlackAttachment();
            slackAttachment.setAuthorName(Optional.ofNullable(pseudoBySvnUserName.get(commit.getAuthor()))
                    .flatMap(author -> Optional.ofNullable(session.findUserByUserName(author)))
                    .map(committer -> "<@" + committer.getId() + ">")
                    .orElse(commit.getAuthor()));
            slackAttachment.setColor("#FC7A25");
            String[] linesOfMessage = commit.getMessage().split("\n");
            slackAttachment.setTitle("#" + commit.getCommitId() + " " + linesOfMessage[0]);
            slackAttachment.setFallback("#" + commit.getCommitId());
            slackAttachment.setTitleLink(sourceViewerUrl + "-" + commit.getCommitId());
            List<String> otherLinesArray = new ArrayList<>(asList(copyOfRange(linesOfMessage, 1, linesOfMessage.length)));
            otherLinesArray.add("(" + commit.getNumberOfUpdatedFiles() + " fichiers touchés)");
            slackAttachment.setText(otherLinesArray.stream().collect(joining("\n")));

            session.sendMessage(sourcecode, "", slackAttachment);

            commitId = commit.getCommitId();
        }));
    }

}
