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
                println " --- > className = $className"
                modifiedClassBytes = modifyClasses(className, sourceClassBytes)
            }
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes)
            } else {
                jarOutputStream.write(modifiedClassBytes)
            }
            jarOutputStream.closeEntry()
        }
//            Log.info("${hexName} is modified")
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    static byte[] modifyClasses(String className, byte[] srcByteCode) {
//        List<Map<String, Object>> methodMatchMaps = getList(container)
        List<Map<String, Object>> methodMatchMaps = new ArrayList<>()
        byte[] classBytesCode = null
//        if (methodMatchMaps) {
        try {
            println("--> start modifying ${className}")
            classBytesCode = modifyClass(srcByteCode, methodMatchMaps)
            println("--> revisit modified ${className}")
            VisitClassMethod(classBytesCode, methodMatchMaps)
            println("--> finish modifying ${className}")
            return classBytesCode
        } catch (Exception e) {
            e.printStackTrace()
        }
//        }
        if (classBytesCode == null) {
            classBytesCode = srcByteCode
        }
        return classBytesCode
    }

    static byte[] modifyClass(byte[] srcClass, List<Map<String, Object>> modifyMatchMaps) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor adapter = new ClassFilterVisitor(classWriter, modifyMatchMaps)
        ClassReader cr = new ClassReader(srcClass)
        cr.accept(adapter, 0)
        return classWriter.toByteArray()
    }

    static void VisitClassMethod(byte[] srcClass, List<Map<String, Object>> modifyMatchMaps) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassFilterVisitor visitor = new ClassFilterVisitor(classWriter, modifyMatchMaps)
        visitor.onlyVisit = true
        ClassReader cr = new ClassReader(srcClass)
        cr.accept(visitor, 0)
    }

    static class ClassFilterVisitor extends ClassVisitor implements Opcodes {
//        private String className
        private List<Map<String, Object>> methodMatchMaps
        public boolean onlyVisit = false

        ClassFilterVisitor(
                final ClassVisitor cv, List<Map<String, Object>> methodMatchMaps) {
            super(Opcodes.ASM5, cv)
//            this.className = className
            this.methodMatchMaps = methodMatchMaps
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
            if ("URL_CLASS_NAME" == name) {
                println " ++++++++++++  找到了 要替换了 +++++++++++++"
                value = "TEST_URL"
            }
            if ("URL_JSON_FILE" == name) {
                println " ++++++++++++  找到了 要替换了 URL_JSON_FILE+++++++++++++"
                value = "testJsonFile"
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
            if (!onlyVisit) {
                println("* visitMethod *" + " , " + access + " , " + name + " , " + desc + " , " + signature + " , " + exceptions)
            }
            super.visitMethod(access, name, desc, signature, exceptions)
        }

    }

}