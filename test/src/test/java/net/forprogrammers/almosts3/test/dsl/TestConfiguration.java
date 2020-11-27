package net.forprogrammers.almosts3.test.dsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestConfiguration {
    private final TestHelper testHelper;
    private final List<TestFile> files;
    private final List<TestUser> users;

    public TestConfiguration(TestHelper testHelper,
                             List<TestFile> files,
                             List<TestUser> users) {
        this.testHelper = testHelper;
        this.files = files;
        this.users = users;
        Set<TestUser> allUsers = Stream.concat(
                files.stream().map(TestFile::getAccessibleFor).flatMap(Collection::stream),
                users.stream()
        ).collect(Collectors.toSet());
        testHelper.createUsers(allUsers);
        testHelper.createFiles(files);
    }

    public TestFile getFile() {
        return files.get(0);
    }

    public TestUser getUser() {
        return users.get(0);
    }

    public static Builder builder(TestHelper testHelper) {
        return new Builder(testHelper);
    }

    public static class Builder {
        private final TestHelper testHelper;
        private List<TestFile> files = new ArrayList<>();
        private List<TestUser> users = new ArrayList<>();

        public Builder(TestHelper testHelper) {
            this.testHelper = testHelper;
        }

        public Builder withFiles(List<TestFile> files) {
            this.files.clear();
            this.files.addAll(files);
            return this;
        }

        public Builder withCreatedUsers(List<TestUser> createdUsers) {
            this.users.clear();
            this.users.addAll(createdUsers);
            return this;
        }

        public Builder withFile(TestFile file) {
            this.files.add(file);
            return this;
        }

        public Builder withUser(TestUser user) {
            this.users.add(user);
            return this;
        }

        public TestConfiguration setup() {
            return new TestConfiguration(testHelper, files, users);
        }
    }
}
