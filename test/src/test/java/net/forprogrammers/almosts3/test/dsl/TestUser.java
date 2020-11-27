package net.forprogrammers.almosts3.test.dsl;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Objects;

public class TestUser {
    public static final TestUser ANONYMOUS = TestUser.builder().withUsername("anonymous").build();
    private final String username;
    private final String name;
    private final String surname;

    public TestUser(String username, String name, String surname) {
        this.username = username;
        this.name = name;
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestUser testUser = (TestUser) o;
        return Objects.equals(username, testUser.username) && Objects.equals(name, testUser.name) && Objects.equals(surname, testUser.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, name, surname);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username = RandomStringUtils.randomAlphabetic(5);
        private String name = RandomStringUtils.randomAlphabetic(10);
        private String surname = RandomStringUtils.randomAlphabetic(10);

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public TestUser build() {
            return new TestUser(username, name, surname);
        }
    }
}
