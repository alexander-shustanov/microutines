package yaroutine;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuspendableConverter {
    private final Method runMethod;

    private ClassLoader classLoader;

    private ClassWriter classWriter;
    private ClassReader classReader;

    private Class<?> currentClass;

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
//        Map<MethodId, SuspendableInfoMethodCollector> callInfo = collectYieldCallInfo(classReader, runMethod);
//        SuspendableInfoMethodCollector runMethodInfo = callInfo.values().iterator().next();

        implementContinuation(/*runMethodInfo*/);
    }

    private void implementContinuation() {
        ClassNode classNode = new ClassNode(Opcodes.ASM7);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        MethodNode method = classNode.methods.stream()
                .filter(methodNode -> methodNode.name.equals("run") && (methodNode.access & Opcodes.ACC_BRIDGE) == 0)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to find method to convert"));
        method.signature = null;

        method.localVariables.removeIf(localVariableNode -> localVariableNode.index != 0);
        method.desc = method.desc.replace("(", "(Ljava/lang/Object;");
//        method.desc = "(Ljava/lang/Object;Lyaroutine/Scope;)V";
        method.maxLocals++;
        method.instructions.forEach(node -> {
            if (node instanceof VarInsnNode) {
                VarInsnNode varInsNode = (VarInsnNode) node;
                if (varInsNode.var > 0) varInsNode.var++;
            }
            if (node instanceof IincInsnNode) {
                ((IincInsnNode) node).var++;

            }
            if (node instanceof FrameNode) {
                ArrayList<Object> locals = new ArrayList<>(((FrameNode) node).local);
                locals.add(1, "java/lang/Object");
                ((FrameNode) node).local = locals;
            }
        });

        MethodNode sortedVars = newMethodFrom(method);
        method.accept(new AnalyzerAdapter(runMethod.getDeclaringClass().getName(), method.access, method.name, method.desc, sortedVars));


        SuspendableInfoMethodCollector suspendableInfo = new SuspendableInfoMethodCollector(classLoader, runMethod.getDeclaringClass().getName(), sortedVars.access, sortedVars.name, sortedVars.desc);
        sortedVars.accept(suspendableInfo);

        MethodNode resultMethod = newMethodFrom(method);
        resultMethod.desc = "(Ljava/lang/Object;)Ljava/lang/Object;";

        method.accept(new SuspendableMethodConverter(classLoader, currentClass, resultMethod, suspendableInfo, method.access, method.name, method.desc));

        method.instructions.forEach(node -> {
            if (node instanceof FrameNode) {
                method.instructions.remove(node);
            }
        });

        ClassNode resultClass = new ClassNode();
        classReader.accept(new ClassVisitor(Opcodes.ASM7, resultClass) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                access |= Opcodes.ACC_PUBLIC;
                String[] newInterfaces = new String[interfaces.length + 1];
                System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
                newInterfaces[newInterfaces.length - 1] = "yaroutine/core/Continuation";
                signature += "Lyaroutine/core/Continuation;";
                super.visit(version, access, name, signature, superName, newInterfaces);
            }

            @Override
            public void visitEnd() {
                List<SuspendInfo.Field> fields = suspendableInfo.getSuspendInfos().stream()
                        .flatMap(suspendInfo -> Stream.concat(suspendInfo.getVariableMappings().stream(), suspendInfo.getStackMappings().stream()))
                        .map(varToFieldMapping -> varToFieldMapping.field)
                        .distinct().collect(Collectors.toList());

                for (SuspendInfo.Field field : fields) {
                    if (!field.fieldDescriptor.equals("T"))
                        super.visitField(Opcodes.ACC_PRIVATE, field.fieldName, field.fieldDescriptor, null, null);
                }

                super.visitField(Opcodes.ACC_PRIVATE, "label$S$S", "I", null, null);
                super.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "maxLabel$S$S", "I", null, suspendableInfo.getLabelCount() + 1);

                super.visitEnd();
            }
        }, ClassReader.SKIP_FRAMES);

        resultClass.methods.removeIf(methodNode -> methodNode.name.equals("run"));

        resultClass.methods.add(resultMethod);

        resultClass.accept(classWriter);
    }

    private MethodNode newMethodFrom(MethodNode method) {
        return new MethodNode(Opcodes.ASM7, method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0]));
    }

}
