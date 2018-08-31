package me.xp.gradle.placeholder

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
}