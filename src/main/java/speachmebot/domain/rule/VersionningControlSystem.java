package speachmebot.domain.rule;

import java.util.List;

public interface VersionningControlSystem {

    String getLastCommitId();

    List<Commit> getLastCommits(String commitId);

    class Commit {

        private String commitId;
        private String author;
        private String message;
        private int numberOfUpdatedFiles;

        public Commit(String commitId, String author, String message, int numberOfUpdatedFiles) {
            this.commitId = commitId;
            this.author = author;
            this.message = message;
            this.numberOfUpdatedFiles = numberOfUpdatedFiles;
        }

        String getCommitId() {
            return commitId;
        }

        String getAuthor() {
            return author;
        }

        String getMessage() {
            return message;
        }

        int getNumberOfUpdatedFiles() {
            return numberOfUpdatedFiles;
        }

    }

}
