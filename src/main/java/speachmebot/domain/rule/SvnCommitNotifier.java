package speachmebot.domain.rule;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import speachmebot.ScheduledTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class SvnCommitNotifier implements ScheduledTask {

    private final DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
    private final DateFormat timeFormatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
    private final Map<String, String> pseudoBySvnUserName;
    private final String sourceViewerUrl;
    private final SVNClientManager svnClientManager;
    private final SVNURL url;

    private long currentRevision;

    public SvnCommitNotifier(String url, String userName, String password, String sourceViewerUrl) {
        try {
            this.url = SVNURL.parseURIEncoded(url);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
        this.sourceViewerUrl = sourceViewerUrl;
        this.svnClientManager = SVNClientManager.newInstance(new DefaultSVNOptions(), userName, password);
        this.pseudoBySvnUserName = new HashMap<>();
        this.pseudoBySvnUserName.put("achappron", "alexis");
        this.pseudoBySvnUserName.put("ccharmet", "mcharmet");
        this.pseudoBySvnUserName.put("fdurieux", "florian");
        this.pseudoBySvnUserName.put("fgloppe", "frederic");
        this.pseudoBySvnUserName.put("jrouet", "jeremy");
        this.pseudoBySvnUserName.put("lleroux", "loic");
        this.pseudoBySvnUserName.put("slemerdy", "sebastian");
        this.pseudoBySvnUserName.put("snoulet", "sylvain");

        svnLog(SVNRevision.HEAD, null, null, 1L, logEntry -> currentRevision = logEntry.getRevision());
    }

    @Override
    public String name() {
        return "SvnCommitNotifier";
    }

    @Override
    public void run(SlackSession session) {
        svnLog(null, SVNRevision.create(currentRevision), SVNRevision.HEAD, 10L, logEntry -> {
            if (logEntry.getRevision() == currentRevision) {
                return;
            }
            Optional.ofNullable(session.findChannelByName("magicians")).ifPresent(magicians -> {
                SlackAttachment slackAttachment = new SlackAttachment();
                slackAttachment.setAuthorName(Optional.ofNullable(pseudoBySvnUserName.get(logEntry.getAuthor()))
                        .flatMap(author -> Optional.ofNullable(session.findUserByUserName(author)))
                        .map(committer -> "<@" + committer.getId() + ">")
                        .orElse(logEntry.getAuthor()));
                slackAttachment.setColor("#FC7A25");
                String[] linesOfMessage = logEntry.getMessage().split("\n");
                slackAttachment.setTitle("#" + logEntry.getRevision() + " " + linesOfMessage[0]);
                slackAttachment.setPretext("nouveau commit dans svn" +
                        " " + (sameDay(logEntry.getDate()) ? "à " + timeFormatter.format(logEntry.getDate()) : "le " + dateFormatter.format(logEntry.getDate())) +
                        " (" + logEntry.getChangedPaths().size() + " fichiers touchés)");
                slackAttachment.setTitleLink(sourceViewerUrl + "-" + logEntry.getRevision());
                String otherLines = stream(copyOfRange(linesOfMessage, 1, linesOfMessage.length)).collect(joining("\n"));
                if (!otherLines.isEmpty()) {
                    slackAttachment.setText(otherLines);
                }
                session.sendMessage(magicians, "", slackAttachment);
                currentRevision = logEntry.getRevision();
            });

        });
    }

    private boolean sameDay(Date date) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate from = LocalDate.from(ZonedDateTime.ofInstant(date.toInstant(), zone));
        LocalDate to = LocalDate.now(zone);
        return from.isEqual(to);
    }

    private void svnLog(SVNRevision pegRevision, SVNRevision startRevision, SVNRevision endRevision, long limit, ISVNLogEntryHandler isvnLogEntryHandler) {
        try {
            svnClientManager.getLogClient().doLog(this.url, new String[0], pegRevision, startRevision, endRevision, true, true, limit, isvnLogEntryHandler);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

}
