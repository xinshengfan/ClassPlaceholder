# ClassPlaceholder
动态替换jar包及java源代码中的值。在jar包或java文件以占位符的形式定义常量。在引入jar包时动态替换
原字符串。在java源文件中同样以占位符的形式定义常量。在generateBuildConfig
任务后会自动修改指定的值。

## 使用

1. 引入仓库：
    ```
        allprojects {
            repositories {
        	...
        	maven { url 'https://jitpack.io' }
        }
        }
    ```

2. 引入插件：
    ```
         dependencies {
                classpath 'com.android.tools.build:gradle:3.1.4'

                classpath 'com.github.xinshengfan:placeholder:1.0.0-SNAPSHOT'

                // NOTE: Do not place your application dependencies here; they belong
                // in the individual module build.gradle files
            }
    ```

3. 要替换的代码中使用占位符定义要替换的值，如：
    ```
        public static final String TEST_PUBLIC = "${public}";
            private static final String TEST_PRIVATE = "${private}";
    ```

4. 在主项目的build.gradle文件中添加`placeholder`插件，定义在替换的值及文件名，如：
    ```
        apply plugin: 'placeholder'

        placeholders {
            addholder {
                //is modify source java file
                isModifyJava = true
                //modify file name
                classFile = "me/xp/gradle/classplaceholder/AppConfig.java"
                //replace name and value
                values = ['public' : 'AppConfigPubic',
                          'private': 'AppConfigPrivate',
                          'field'  : 'AppConfigField']
            }
            addholder {
                isModifyJava = false
                classFile = "me/xp/gradle/jarlibrary/JarConfig.class"
                values = ['config': 'JarConfigPubic']
            }
        }
    ```

5. 若修改替换值，需要先执行`clean`命令，再build。