package me.xp.gradle.placeholder

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 查找并替换
 */
class ModifyUtils {

    static File modifyJar(File jarFile, File tempDir, boolean nameHex) {
        /**
         * 读取原jar
         */
        def file = new JarFile(jarFile)
        /** 设置输出到的jar */
        def hexName = ""
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = file.getInputStream(jarEntry)

            String entryName = jarEntry.getName()
            String className

            ZipEntry zipEntry = new ZipEntry(entryName)

            jarOutputStream.putNextEntry(zipEntry)

            byte[] modifiedClassBytes = null
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
            if (entryName.endsWith(".class")) {
                className = entryName.replace(".class", "")
                modifiedClassBytes = modifyClasses(className, sourceClassBytes)
            }
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes)
            } else {
                jarOutputStream.write(modifiedClassBytes)
            }
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    static byte[] modifyClasses(String className, byte[] srcByteCode) {
        Map<String, String> modifyMap = Utils.modifyMap(className)
        byte[] classBytesCode = null
        if (modifyMap) {
            try {
                println("--> start modifying ${className}")
                classBytesCode = modifyClass(srcByteCode, modifyMap, false)
                println("--> revisit modified ${className}")
                modifyClass(srcByteCode, modifyMap, true)
                println("--> finish modifying ${className}")
                return classBytesCode
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        if (classBytesCode == null) {
            classBytesCode = srcByteCode
        }
        return classBytesCode
    }

    static byte[] modifyClass(byte[] srcClass, Map<String, String> modifyMap, boolean isOnlyVisit) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor adapter = new ClassFilterVisitor(classWriter, modifyMap)
        adapter.isOnlyVisit = isOnlyVisit
        ClassReader cr = new ClassReader(srcClass)
        cr.accept(adapter, 0)
        return classWriter.toByteArray()
    }

    static class ClassFilterVisitor extends ClassVisitor implements Opcodes {
        private Map<String, String> modifyMap
        public boolean isOnlyVisit = false

        ClassFilterVisitor(
                final ClassVisitor cv, Map<String, String> modifyMap) {
            super(Opcodes.ASM5, cv)
            this.modifyMap = modifyMap
        }

        @Override
        void visitEnd() {
            println('* visitEnd *')
            super.visitEnd()
        }

        @Override
        void visitAttribute(Attribute attribute) {
            println('* visitAttribute *' + "," + attribute + "," + attribute.type + "," + attribute.metaClass + "," + attribute.metaPropertyValues + "," + attribute.properties)
            super.visitAttribute(attribute)
        }

        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            println('* visitAnnotation *' + " , " + desc + " , " + visible)
            return super.visitAnnotation(desc, visible)
        }

        @Override
        void visitInnerClass(String name, String outerName,
                             String innerName, int access) {
            println('* visitInnerClass *' + " , " + name + " , " + outerName + " , " + innerName + " , " + access)
            super.visitInnerClass(name, outerName, innerName, access)
        }

        @Override
        void visitOuterClass(String owner, String name, String desc) {
            println('* visitOuterClass *' + " , " + owner + " , " + name + " , " + desc)
            super.visitOuterClass(owner, name, desc)
        }

        @Override
        void visitSource(String source, String debug) {
            println('* visitSource *' + " , " + source + " , " + debug)
            super.visitSource(source, debug)
        }

        @Override
        FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            println('* visitField *' + " , " + access + " , " + name + " , " + desc + " , " + signature + " , " + value)
            if (!isOnlyVisit) {
                modifyMap.each { k, v ->
                    def matchValue = "\${$k}"
                    println "matchValue = $matchValue , value = $value --> ${matchValue == value}"
                    if (matchValue == value) {
                        println " ++++++++++++  find $k replace $v +++++++++++++"
                        value = v
                    }
                }
            }
            return super.visitField(access, name, desc, signature, value)
        }

        @Override
        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            println('* visit *' + " , " + access + " , " + name + " , " + signature + " , " + superName + " , " + interfaces)
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        public MethodVisitor visitMethod(int access, String name,
                                         String desc, String signature, String[] exceptions) {
            println("* visitMethod *" + " , " + access + " , " + name + " , " + desc + " , " + signature + " , " + exceptions)
            super.visitMethod(access, name, desc, signature, exceptions)
        }

    }

}