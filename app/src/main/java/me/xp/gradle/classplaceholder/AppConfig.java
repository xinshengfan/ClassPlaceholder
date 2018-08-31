package me.xp.gradle.classplaceholder;

public class AppConfig {
    public static final String TEST_PUBLIC = "AppConfigPubic";
    private static final String TEST_PRIVATE = "AppConfigPrivate";
    private String TEST_FEILD = "AppConfigField";
    private String field = "field";


    @Override
    public String toString() {
        return "AppConfig:\n" +
                "TEST_FEILD=" + TEST_FEILD + '\n' +
                ", TEST_PUBLIC=" + TEST_PUBLIC + '\n' +
                ", TEST_PRIVATE=" + TEST_PRIVATE + '\n' +
                ", field=" + field + '\n';
    }
}
