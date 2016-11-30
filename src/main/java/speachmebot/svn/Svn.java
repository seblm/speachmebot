package speachmebot.svn;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import speachmebot.domain.rule.VersionningControlSystem;

import java.util.ArrayList;
import java.util.List;

public class Svn implements VersionningControlSystem {

    private final SVNClientManager svnClientManager;

    private SVNURL url;

    public Svn(String userName, String password, String url) {
        try {
            this.url = SVNURL.parseURIEncoded(url);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
        this.svnClientManager = SVNClientManager.newInstance(new DefaultSVNOptions(), userName, password);
    }

    @Override
    public String getLastCommitId() {
        final String[] currentRevision = new String[1];
        svnLog(SVNRevision.HEAD, null, null, 1L, logEntry -> currentRevision[0] = Long.toString(logEntry.getRevision()));
        return currentRevision[0];
    }

    @Override
    public List<Commit> getLastCommits(String commitId) {
        List<Commit> commits = new ArrayList<>();
        long revisionNumber = Long.parseLong(commitId);
        svnLog(null, SVNRevision.create(revisionNumber), SVNRevision.HEAD, 10L, logEntry -> {
            if (logEntry.getRevision() == revisionNumber) {
                return;
            }
            commits.add(new Commit(
                    Long.toString(logEntry.getRevision()),
                    logEntry.getAuthor(),
                    logEntry.getMessage(),
                    logEntry.getChangedPaths().size()
            ));
        });
        return commits;
    }

    private void svnLog(SVNRevision pegRevision, SVNRevision startRevision, SVNRevision endRevision, long limit, ISVNLogEntryHandler isvnLogEntryHandler) {
        try {
            svnClientManager.getLogClient().doLog(url, new String[0], pegRevision, startRevision, endRevision, true, true, limit, isvnLogEntryHandler);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

}
