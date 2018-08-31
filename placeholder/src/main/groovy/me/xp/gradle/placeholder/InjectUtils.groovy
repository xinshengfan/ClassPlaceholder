package me.xp.gradle.placeholder

import com.android.build.api.transform.Context
import groovy.io.FileType
import javassist.ClassPool
import javassist.CtField
import org.apache.commons.io.IOUtils

/**
 * 查找并替换
 */
class InjectUtils {
    static ClassPool classPool = ClassPool.default
    static HashMap<String, File> modifyMap = new HashMap<>();

    static HashMap<String, File> replaceInDir(Context context, String path, PlaceholderExtension extension) {
        classPool.appendClassPath(path)
        modifyMap.clear()
        File dir = new File(path)
        println "repace dir --> ${dir.name} >> ${path}"
        if (dir.isDirectory()) {
            dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                File classFile ->
                    def filePath = classFile.absolutePath
                    if (!filePath.contains('R$')
                            && !filePath.contains('R.class')
                            && !filePath.contains("BuildConfig.class")) {
                        //自己项目下和文件
                        println "遍历到的文件 filePath = $filePath"
                        if (filePath.endsWith("G.class")) {
                            def ctClass = classPool.getCtClass("me.xp.gradle.classplaceholder.G")
                            println "ctClass = $ctClass"
                            if (ctClass.isFrozen())
                                ctClass.defrost()
                            ctClass.declaredFields.each {
                                println "it = ${it.name}"
                            }
//                            ctClass.addField(CtField.make("public static final int TEST = 1;", ctClass))
                            println "删除原属性--->"
                            def field = ctClass.getField("URL_JSON_FILE")
                            println "field = $field --> ${field.name}"
                            ctClass.removeField(field)

                            println "新建一个原属性-->"
                            ctClass.addField(CtField.make("public static final String URL_JSON_FILE = \"aaaaaa\";", ctClass))
                            println "---> path = $path"
                            ctClass.writeFile(path)
                            ctClass.detach()
                        }
                    }
            }

        }
        return modifyMap
    }

    static File modifyClassFile(File dir, File classFile, File tempDir) {
        def modified = null
        def className = classFile.absolutePath.replace(dir.absolutePath + File.separator, "")
        className = className.replace(".class", "")
        println "className = $className"
        byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
        byte[] modifiedClassBytes = ModifyUtils.modifyClasses(className, sourceClassBytes)
        if (modifiedClassBytes) {
            println "dir = [$dir], classFile = [$classFile], tempDir = [$tempDir]"
            modified = new File(tempDir, className.replace('.', '') + '.class')
            if (modified.exists()) {
                modified.delete()
            }
            def tempModifiedDir = modified.parent
            def tempModifiedFile = new File(tempModifiedDir)
            println "tempModifiedDir = $tempModifiedDir , is exists >> ${tempModifiedFile.exists()}"
            if (!tempModifiedFile.exists()) {
                tempModifiedFile.mkdirs()
            }
            new FileOutputStream(modified).write(modifiedClassBytes)
        }

        return modified

    }

    static File replaceInJar(Context context, File file) {
        if (Utils.isNeedModifyJar(file)) {
            println "需要修改jar file >> ${file.path}"
            return ModifyUtils.modifyJar(file, context.temporaryDir, true)

        }
        return null
    }


}