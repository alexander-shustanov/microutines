package joroutine;

import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SuspendableConverter {
    private final Method runMethod;

    private ClassLoader classLoader;

    private ClassWriter classWriter;
    private ClassReader classReader;

    private Class currentClass;

    public SuspendableConverter(ClassLoader classLoader, ClassWriter classWriter, ClassReader classReader, Class currentClass) {
        this.classLoader = classLoader;
        this.classWriter = classWriter;
        this.classReader = classReader;
        this.currentClass = currentClass;

        this.runMethod = findMethodToProcess(currentClass);
    }

    private Method findMethodToProcess(Class currentClass) {
        return Arrays.stream(currentClass.getDeclaredMethods())
                .filter(method -> method.getName().equals("run") && !method.isBridge()).findFirst()
                .orElseThrow(() -> new RuntimeException(""));
    }

    public void process() {
        Map<MethodId, SuspendableInfoMethodCollector> callInfo = collectYieldCallInfo(classReader, runMethod);
        SuspendableInfoMethodCollector runMethodInfo = callInfo.values().iterator().next();

        implementContinuation(runMethodInfo);
    }

    private void implementContinuation(SuspendableInfoMethodCollector runMethodInfo) {
        classReader.accept(new ClassVisitor(Opcodes.ASM7, classWriter) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                access |= Opcodes.ACC_PUBLIC;
                String[] newInterfaces = new String[interfaces.length + 1];
                System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
                newInterfaces[newInterfaces.length - 1] = "joroutine/Continuation";
                signature += "Ljoroutine/Continuation;";
                super.visit(version, access, name, signature, superName, newInterfaces);
            }



            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (name.equals("run")) {
                    if ((access & Opcodes.ACC_BRIDGE) != 0)
                        return new MethodVisitor(api) { // delete bridge method
                        };

                    return new SuspendableMethodConverter(super.visitMethod(access, name, "()Ljava/lang/Object;", null, exceptions), classLoader, currentClass, runMethodInfo);
                }
//
//                if (name.equals("<init>")) {
//                    return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
//                        @Override
//                        public void visitCode() {
//                            super.visitCode();
//                            super.visitVarInsn(Opcodes.ALOAD, 0);
//                            super.visitIincInsn(Opcodes.BIPUSH, runMethodInfo.getLabelCount() + 1);
//                            super.visitFieldInsn(Opcodes.PUTFIELD, currentClass.getName().replace('.', '/'), "maxLabel$S$S", "I");
//                        }
//                    };
//                }

                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }

            @Override
            public void visitEnd() {
                runMethodInfo.getVariables().stream().filter(localVariable -> !localVariable.name.equals("this")).forEach(localVariable -> {
                    super.visitField(Opcodes.ACC_PRIVATE, localVariable.name + "$S", localVariable.descriptor, null, null);
                });
                super.visitField(Opcodes.ACC_PRIVATE, "label$S$S", "I", null, null);
                super.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC , "maxLabel$S$S", "I", null, runMethodInfo.getLabelCount() + 1);

                super.visitEnd();
            }
        }, ClassReader.SKIP_FRAMES);
    }

    private Map<MethodId, SuspendableInfoMethodCollector> collectYieldCallInfo(ClassReader classReader, Method runMethod) {
        Map<MethodId, SuspendableInfoMethodCollector> yieldCalls = new HashMap<>();
        classReader.accept(new ClassVisitor(Opcodes.ASM7) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (name.equals("run") && (access & Opcodes.ACC_BRIDGE) == 0 && Objects.equals(signature, Utils.getSignature(runMethod))) {
                    SuspendableInfoMethodCollector counter = new SuspendableInfoMethodCollector(classLoader);
                    yieldCalls.put(new MethodId(access, name, descriptor, signature, exceptions), counter);
                    return counter;
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, ClassReader.EXPAND_FRAMES);
        return yieldCalls;
    }
}
