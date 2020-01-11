package microutine;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

public class SuspendableMethodConverter extends MethodVisitor {
    private static final int RESUME_WITH_PARAMETER = 1;
    private static final int THIS_VAR_INDEX = 0;
    public static final String LABEL_FIELD_NAME = "label$S$S";

    private final ClassLoader classLoader;
    private final int numLabels;
    private final Label[] labels;
    private final String myClassJvmName;
    private final int labelVarIndex;
    private final List<SuspendInfo> suspendInfos;

    private SuspendInfoCollector methodInfo;

    int suspensionNumber = 0;

    private Map<String, Integer> doubleSwapVarIndex = new HashMap<>();

    int nextVarIndex;


    public SuspendableMethodConverter(ClassLoader classLoader, Class<?> currentClass, MethodVisitor methodVisitor, SuspendInfoCollector methodInfo) {
        super(Opcodes.ASM7, methodVisitor);
        this.classLoader = classLoader;
        this.methodInfo = methodInfo;

        myClassJvmName = currentClass.getName().replace('.', '/');

        suspendInfos = methodInfo.getSuspendInfos();
        this.numLabels = methodInfo.getLabelCount();
        this.labelVarIndex = methodInfo.getMaxLocals();
        nextVarIndex = labelVarIndex + 1;

        labels = new Label[numLabels];
        for (int i = 0; i < numLabels; i++) {
            labels[i] = new Label();
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();

        Label startLabel = new Label();

        super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
        super.visitFieldInsn(Opcodes.GETFIELD, myClassJvmName, LABEL_FIELD_NAME, "I");
        super.visitVarInsn(Opcodes.ISTORE, labelVarIndex);

        super.visitVarInsn(Opcodes.ILOAD, labelVarIndex);
        super.visitIntInsn(Opcodes.BIPUSH, 0);
        super.visitJumpInsn(Opcodes.IF_ICMPEQ, startLabel);

        for (int i = 0; i < numLabels; i++) {
            super.visitVarInsn(Opcodes.ILOAD, labelVarIndex);
            super.visitIntInsn(Opcodes.BIPUSH, i + 1);
            super.visitJumpInsn(Opcodes.IF_ICMPEQ, labels[i]);
        }

        super.visitTypeInsn(Opcodes.NEW, "microutine/core/ContinuationEndException");
        super.visitInsn(Opcodes.DUP);
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, "microutine/core/ContinuationEndException", "<init>", "()V", false);
        super.visitInsn(Opcodes.ATHROW);

        super.visitLabel(startLabel);

        restoreFrame();
        suspensionNumber++;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        boolean suspendPoint = Utils.isSuspendPoint(classLoader, owner, name);

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if (suspendPoint) {
            boolean voidReturn = Type.getReturnType(descriptor) == Type.VOID_TYPE;

            if (!voidReturn) {
                super.visitInsn(Opcodes.POP);
            }

            super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
            super.visitIntInsn(Opcodes.BIPUSH, suspensionNumber);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, LABEL_FIELD_NAME, "I");

            saveFrame();

            suspend();
            super.visitLabel(labels[suspensionNumber - 1]);
            restoreFrame();
            if (!voidReturn) {
                super.visitVarInsn(Opcodes.ALOAD, RESUME_WITH_PARAMETER);
            }
            suspensionNumber++;
        }
    }

    private void suspend() {
        super.visitFieldInsn(Opcodes.GETSTATIC, "microutine/core/Continuation", "SUSPEND", "Ljava/lang/Object;");
        super.visitInsn(Opcodes.ARETURN);
    }

    private void restoreFrame() {
        SuspendInfo suspendInfo = suspendInfos.get(suspensionNumber);
        for (SuspendInfo.Mapping mapping : suspendInfo.getVariableMappings()) {
            super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
            super.visitFieldInsn(Opcodes.GETFIELD, myClassJvmName, mapping.field.fieldName, mapping.field.fieldDescriptor);

            int opcode;
            switch (mapping.field.fieldDescriptor) {
                case "I":
                case "Z":
                case "S":
                case "C":
                    opcode = Opcodes.ISTORE;
                    break;
                case "J":
                    opcode = Opcodes.LSTORE;
                    break;
                case "F":
                    opcode = Opcodes.FSTORE;
                    break;
                case "D":
                    opcode = Opcodes.DSTORE;
                    break;
                default:
                    opcode = Opcodes.ASTORE;
                    break;
            }
            super.visitVarInsn(opcode, mapping.index);
        }

        for (SuspendInfo.Mapping mapping : suspendInfo.getStackMappings()) {
            super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
            super.visitFieldInsn(Opcodes.GETFIELD, myClassJvmName, mapping.field.fieldName, mapping.field.fieldDescriptor);
        }
    }

    private void saveFrame() {
        SuspendInfo suspendInfo = suspendInfos.get(suspensionNumber);
        for (SuspendInfo.Mapping mapping : suspendInfo.getVariableMappings()) {
            int opcode;
            switch (mapping.field.fieldDescriptor) {
                case "I":
                case "Z":
                case "S":
                case "C":
                    opcode = Opcodes.ILOAD;
                    break;
                case "J":
                    opcode = Opcodes.LLOAD;
                    break;
                case "F":
                    opcode = Opcodes.FLOAD;
                    break;
                case "D":
                    opcode = Opcodes.DLOAD;
                    break;
                default:
                    opcode = Opcodes.ALOAD;
                    break;
            }
            super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
            super.visitVarInsn(opcode, mapping.index);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, mapping.field.fieldName, mapping.field.fieldDescriptor);
        }

        List<SuspendInfo.Mapping> stackMappings = new ArrayList<>(suspendInfo.getStackMappings());
        Collections.reverse(stackMappings);
        for (SuspendInfo.Mapping mapping : stackMappings) {
            switch (mapping.field.fieldDescriptor) {
                case "J":
                    super.visitVarInsn(Opcodes.LSTORE, getSwapIndex(mapping.field.fieldDescriptor));
                    super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
                    super.visitVarInsn(Opcodes.LLOAD, getSwapIndex(mapping.field.fieldDescriptor));
                    break;
                case "D":
                    super.visitVarInsn(Opcodes.DSTORE, getSwapIndex(mapping.field.fieldDescriptor));
                    super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
                    super.visitVarInsn(Opcodes.DLOAD, getSwapIndex(mapping.field.fieldDescriptor));
                    break;
                default:
                    super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
                    super.visitInsn(Opcodes.SWAP);
                    break;
            }

            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, mapping.field.fieldName, mapping.field.fieldDescriptor);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
            super.visitIntInsn(Opcodes.BIPUSH, methodInfo.getLabelCount() + 1);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, LABEL_FIELD_NAME, "I");
            super.visitInsn(Opcodes.ACONST_NULL);
            super.visitInsn(Opcodes.ARETURN);
        } else if (opcode == Opcodes.ARETURN || opcode == Opcodes.DRETURN || opcode == Opcodes.LRETURN || opcode == Opcodes.FRETURN || opcode == Opcodes.IRETURN) {
            super.visitVarInsn(Opcodes.ALOAD, THIS_VAR_INDEX);
            super.visitIntInsn(Opcodes.BIPUSH, methodInfo.getLabelCount() + 1);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, LABEL_FIELD_NAME, "I");
            super.visitInsn(opcode);
        } else {
            super.visitInsn(opcode);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitLocalVariable("label", "I", null, new Label(), new Label(), labelVarIndex);
        super.visitMaxs(maxStack, maxLocals);
    }

    private int getSwapIndex(String descriptor) {
        if (doubleSwapVarIndex.containsKey(descriptor))
            return doubleSwapVarIndex.get(descriptor);

        doubleSwapVarIndex.put(descriptor, nextVarIndex++);
        return doubleSwapVarIndex.get(descriptor);
    }
}
