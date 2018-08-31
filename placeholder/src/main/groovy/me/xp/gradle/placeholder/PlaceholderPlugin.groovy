package me.xp.gradle.placeholder

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class PlaceholderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        //build.gradle中使用的扩展
        project.extensions.create('placeholders', Placeholders, project)

        def android = project.extensions.getByType(AppExtension)
        def transform = new ClassPlaceholderTransform(project)
        android.registerTransform(transform)

        //执行修改java源代码的任务
        android.applicationVariants.all { variant ->

            def holders = project.placeholders
            if (holders == null || holders.placeholders == null) {
                println "not add place holder extension!!!"
                return
            }
            ExtensionManager.instance().cacheExtensions(holders.placeholders)
//            println "holders = ${holders.toString()} --> ${holders.placeholders}"

            //获取到scope,作用域
            def variantData = variant.variantData
            def scope = variantData.scope

            //创建一个task
            def createTaskName = scope.getTaskName("modify", "PlaceholderPlugin")
            println "createTaskName = $createTaskName"
            def createTask = project.task(createTaskName)
            //设置task要执行的任务
            createTask.doLast {
                modifySourceFile(project, holders.placeholders)
            }
            //设置task依赖于生成BuildConfig的task，在其之后生成我们的类
            String generateBuildConfigTaskName = variant.getVariantData().getScope().getGenerateBuildConfigTask().name
            def generateBuildConfigTask = project.tasks.getByName(generateBuildConfigTaskName)
            if (generateBuildConfigTask) {
                createTask.dependsOn generateBuildConfigTask
                generateBuildConfigTask.finalizedBy createTask
            }
        }


    }

    static void modifySourceFile(Project project, List<PlaceholderExtension> placeholders) {
        project.allprojects {
            placeholders.each { placeholder ->
                if (!placeholder.isModifyJava) return
                def map = placeholder.values
                map.each { k, v ->
                    def matchKey = String.format("\${%s}", k)
                    println "k = $k , v = $v , matchKey=$matchKey"
                    def dir = String.format("src/main/java/%s", Utils.classFileDir(placeholder))
                    def className = Utils.classFileNameWithJava(placeholder)
                    println "dir = [$dir], className = [$className]"
                    def f = dir + "/" + className
                    ant.replace(
                            file: f,
                            token: matchKey,
                            value: v
                    ) {
                        fileset(dir: dir, includes: className)
                    }

                }
            }
        }
    }

}