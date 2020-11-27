# Almost s3

## Disclaimer

Goal of this repo is to show approach to testing using in-memory DB and embedded HTTP servers and hitting running app with real HTTP client, as opposed to mocking everything.

Rest of the code is of questionable quality and sanity, but it's there only to provide some background.

I tried to make small, but also reasonably realistic use-case.

## Important part

The whole point is to be able to express tests in readable manner, expressing state of the application in domain specific language, without any notion of how technically this is done (whether some info comes from DB, other services or anywhere else).
For example:

```java
TestConfiguration testConfiguration = testHelper.createNewConfiguration()
        .withFile(TestFile.builder()
                .withAccessibleAnonymously()
                .withAvailableForDownload()
                .build())
        .setup();
```

or more complex:

```java
TestUser user = TestUser.builder().build();
TestConfiguration testConfiguration = testHelper.createNewConfiguration()
        .withFile(TestFile.builder()
                .withAccessibleFor(user)
                .withAvailableForDownload()
                .build())
        .setup();
```

Pretty much any domain specific test object comes with a builder, to avoid any confusion as to "how do I get this object".