package nl.basjes.parse.useragent.servlet.api.graphql;

import nl.basjes.parse.useragent.Version;

public class YauaaGraphQLVersion {

    private static final YauaaGraphQLVersion INSTANCE;

    static {
        INSTANCE = new YauaaGraphQLVersion();
    }

    public static YauaaGraphQLVersion getInstance() {
        return INSTANCE;
    }

    public final String gitCommitId              = Version.GIT_COMMIT_ID;
    public final String gitCommitIdDescribeShort = Version.GIT_COMMIT_ID_DESCRIBE_SHORT;
    public final String buildTimeStamp           = Version.BUILD_TIME_STAMP;
    public final String projectVersion           = Version.PROJECT_VERSION;
    public final String copyright                = Version.COPYRIGHT;
    public final String license                  = Version.LICENSE;
    public final String url                      = Version.URL;
    public final String buildJDKVersion          = Version.BUILD_JDK_VERSION;
    public final String targetJREVersion         = Version.TARGET_JRE_VERSION;
}
