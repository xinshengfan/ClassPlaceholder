package me.xp.gradle.placeholder

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class ClassPlaceholderTransform extends Transform {

    Project project

    ClassPlaceholderTransform(Project project) {
        println "----- 注入 transform 了 ----"
        this.project = project
    }
    /**
     * 设置自定义的Transform对应的Task名称
     * @return
     */
    @Override
    String getName() {
        return "placeholderTransform"
    }
    /**
     * 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
     * @return 这里只处理Class文件
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }
    /**
     * 指定Transform的作用范围
     * @return 所有文件下去寻找
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }
    /**
     * 是否支持增量编译
     * @return false-不支持
     */
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        def outputProvider = transformInvocation.getOutputProvider()
        def inputs = transformInvocation.inputs

        def placeholder = project.extensions.getByType(Placeholders)
        println "placeholders = ${placeholder.placeholders.size()}"

        println "-- start transform ---"

        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput dirInput ->
                // 获取output目录
                def dest = outputProvider.getContentLocation(dirInput.name,
                        dirInput.contentTypes, dirInput.scopes,
                        Format.DIRECTORY)

                File dir = dirInput.file
//                File dir = null
                if (dir) {
                    HashMap<String, File> modifyMap = new HashMap<>()
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                        File classFile ->
                            def isNeedModify = Utils.isNeedModify(classFile.absolutePath)
                            if (isNeedModify) {
                                println " need modify class ${classFile.path}"
                                File modified = InjectUtils.modifyClassFile(dir, classFile, transformInvocation.context.getTemporaryDir())
                                if (modified != null) {
                                    //key为相对路径
                                    modifyMap.put(classFile.absolutePath.replace(dir.absolutePath, ""), modified)
                                }
                            }
                    }

                    modifyMap.entrySet().each {
                        Map.Entry<String, File> entry ->
                            File target = new File(dest.absolutePath + entry.getKey())
                            println "entry --> ${entry.key} target = $target"
                            if (target.exists()) {
                                target.delete()
                            }
                            FileUtils.copyFile(entry.getValue(), target)
                            println "dir = ${dir.absolutePath} "

                            saveModifiedJarForCheck(entry.getValue(), new File(dir.absolutePath + entry.getKey()))
                            entry.getValue().delete()
                    }

                }

//                InjectUtils.replaceInDir(transformInvocation.context, dirInput.file.absolutePath, replaceExtension)

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(dirInput.file, dest)


            }

            input.jarInputs.each { JarInput jarInput ->

                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                def modifyJarFile = InjectUtils.replaceInJar(transformInvocation.context, jarInput.file)
                if (modifyJarFile == null) {
                    modifyJarFile = jarInput.file
//                    println "modifyJarFile = ${modifyJarFile.absolutePath}"
                } else {
                    //文件修改过
                    println "++++ jar modified  >> ${modifyJarFile.absolutePath}"
                    saveModifiedJarForCheck(modifyJarFile, jarInput.file)
                }

                //将输入内容复制到输出
                FileUtils.copyFile(modifyJarFile, dest)
            }
        }

    }


    private static void saveModifiedJarForCheck(File newJar, File oldFile) {
        if (oldFile.exists()) {
            oldFile.delete()
        }
        FileUtils.copyFile(newJar, oldFile)
    }

}