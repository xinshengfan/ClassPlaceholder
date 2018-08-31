package me.xp.gradle.placeholder

import java.util.jar.JarFile

class Utils {

    static String classFileDir(PlaceholderExtension extension) {
        if (extension) {
            def classFile = extension.classFile
            if (classFile) {
                return classFile.substring(0, classFile.lastIndexOf("/"))
            }
        }
        return ""
    }

    static String classFileNameWithClass(PlaceholderExtension extension) {
        return classFileName(extension) + ".class"
    }

    static String classFileNameWithJava(PlaceholderExtension extension) {
        return classFileName(extension) + ".java"
    }

    static String classFileName(PlaceholderExtension extension) {
        if (extension) {
            def classFile = extension.classFile
            if (classFile) {
                def start = classFile.lastIndexOf("/") + 1
                def end = classFile.lastIndexOf(".")
                return classFile.substring(start, end)
            }
        }
        return ""
    }

    static boolean isNeedModify(String filePath) {
        if (!filePath?.trim()) return false
        if (ExtensionManager.instance().extensions.isEmpty()) return false
        ExtensionManager.instance().extensions.any {
            def file = String.format("%s/%s", classFileDir(it), classFileNameWithClass(it))
            return filePath.endsWith(file)
        }
    }

    static boolean isNeedModifyJar(File jarFile) {
        if (!jarFile || ExtensionManager.instance().extensions.isEmpty()) return false
        def modify = false
        def file = new JarFile(jarFile)
        def entries = file.entries()
        while (entries.hasMoreElements()) {
            def entry = entries.nextElement()
            def isModify = ExtensionManager.instance().extensions.any {
                def name = String.format("%s/%s", classFileDir(it), classFileNameWithClass(it))
                if (name == entry.name) {
                    modify = true
                    true
                } else false
            }
            if (isModify) break
        }

        file.close()
        return modify
    }

    static Map<String, String> modifyMap(String className) {
        if (!className?.trim()) return null
        if (ExtensionManager.instance().extensions.isEmpty()) return null
        Map<String, String> values = [:]
        ExtensionManager.instance().extensions.find {
            def name = String.format("%s/%s", classFileDir(it), classFileName(it))
            if (name == className) {
                values.putAll(it.values)
                true
            } else false
        }
        return values
    }
}