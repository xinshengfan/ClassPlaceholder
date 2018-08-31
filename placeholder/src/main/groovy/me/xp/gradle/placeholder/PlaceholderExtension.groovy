package me.xp.gradle.placeholder

import org.gradle.api.Project

class PlaceholderExtension {
    Project project
/**
 * 要替换的文件
 */
    String classFile = ''
    /**
     * 要替换的模板及值，
     * 如：${template}* map->
     *{"template","value"}*/
    Map<String, String> values = [:]

    /**
     * 是否修改项目下的java源文件
     */
    boolean isModifyJava = false


    @Override
    String toString() {
        def sb = new StringBuilder("classFile=$classFile,isModifyJava=$isModifyJava\n")
        values.each { k, v ->
            sb.append("valus>> [$k:$v]\n")
        }
        return sb.toString()
    }

    PlaceholderExtension(Project project) {
        this.project = project
    }
}