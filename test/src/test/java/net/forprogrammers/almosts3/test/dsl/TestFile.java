package net.forprogrammers.almosts3.test.dsl;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestFile {
    private final List<TestUser> accessibleFor;
    private final UUID fileId;
    private final String category;
    private final boolean downloadable;
    private final String content;

    public TestFile(List<TestUser> accessibleFor,
                    UUID fileId, String category,
                    boolean downloadable,
                    String content) {
        this.accessibleFor = accessibleFor;
        this.fileId = fileId;
        this.category = category;
        this.downloadable = downloadable;
        this.content = content;
    }

    public List<TestUser> getAccessibleFor() {
        return accessibleFor;
    }

    public UUID getFileId() {
        return fileId;
    }

    public String getCategory() {
        return category;
    }

    public boolean isDownloadable() {
        return downloadable;
    }

    public String getContent() {
        return content;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<TestUser> accessibleFor = new ArrayList<>();
        private UUID fileId = UUID.randomUUID();
        private String category = RandomStringUtils.randomAlphabetic(5);
        private boolean downloadable = true;
        private String content = RandomStringUtils.randomAlphabetic(100);

        public Builder withAccessibleFor(TestUser user) {
            accessibleFor.add(user);
            return this;
        }

        public Builder withAccessibleFor(List<TestUser> accessibleFor) {
            this.accessibleFor.clear();
            this.accessibleFor.addAll(accessibleFor);
            return this;
        }

        public Builder withFileId(UUID fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder withCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder withUnavailableForDownload() {
            this.downloadable = false;
            return this;
        }

        public Builder withAvailableForDownload() {
            this.downloadable = true;
            return this;
        }

        public Builder withAccessibleAnonymously() {
            this.accessibleFor.add(TestUser.ANONYMOUS);
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public TestFile build() {
            return new TestFile(accessibleFor, fileId, category, downloadable, content);
        }
    }
}
