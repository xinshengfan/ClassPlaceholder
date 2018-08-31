package me.xp.gradle.placeholder

import org.gradle.api.Project

class Placeholders {
    Project project

    Placeholders(Project project) {
        this.project = project
    }
/**
 * 要替换的PlaceholderExtension列表
 */
    Iterable<PlaceholderExtension> placeholders = []

    /**
     * 添加一个扩展对象
     * @param closure
     */
    void addholder(Closure closure) {
        def extension = new PlaceholderExtension(project)
        project.configure(extension, closure)
        println " -- > $extension"
        placeholders.add(extension)
    }

    @Override
    String toString() {
        def sb = new StringBuilder()
        placeholders.each {
            sb.append(">> ${it.toString()}")
        }
        sb.append("\n")
        println sb.toString()
        return sb.toString()
    }
}