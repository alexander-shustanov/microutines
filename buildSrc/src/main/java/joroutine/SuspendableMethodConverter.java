package joroutine;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.List;

public class SuspendableMethodConverter extends MethodVisitor {
    private final ClassLoader classLoader;
    private final int numLabels;
    private final Label[] labels;
    private final String myClassJvmName;
    private final int labelVarIndex;
    private final int thisVarIndex;
    private final List<SuspendInfo> suspendInfos;

    private SuspendableInfoMethodCollector methodInfo;

    int suspensionNumber = 0;

    public SuspendableMethodConverter(ClassLoader classLoader, Class<?> currentClass, MethodVisitor methodVisitor, SuspendableInfoMethodCollector methodInfo, int access, String name, String descriptor) {
        super(Opcodes.ASM7, methodVisitor);
        this.classLoader = classLoader;
        this.methodInfo = methodInfo;

        myClassJvmName = currentClass.getName().replace('.', '/');

        suspendInfos = methodInfo.getSuspendInfos();
        this.numLabels = methodInfo.getLabelCount();
        this.labelVarIndex = methodInfo.getMaxLocals();

        thisVarIndex = 0;


        labels = new Label[numLabels];
        for (int i = 0; i < numLabels; i++) {
            labels[i] = new Label();
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();

        Label startLabel = new Label();

        super.visitVarInsn(Opcodes.ALOAD, thisVarIndex);
        super.visitFieldInsn(Opcodes.GETFIELD, myClassJvmName, "label$S$S", "I");
        super.visitVarInsn(Opcodes.ISTORE, labelVarIndex);

        super.visitVarInsn(Opcodes.ILOAD, labelVarIndex);
        super.visitIntInsn(Opcodes.BIPUSH, 0);
        super.visitJumpInsn(Opcodes.IF_ICMPLE, startLabel);

        for (int i = 0; i < numLabels; i++) {
            super.visitVarInsn(Opcodes.ILOAD, labelVarIndex);
            super.visitIntInsn(Opcodes.BIPUSH, i + 1);
            super.visitJumpInsn(Opcodes.IF_ICMPLE, labels[i]);
        }

        doReturn();

        super.visitLabel(startLabel);


        restoreFrame();
        suspensionNumber++;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        boolean suspendPoint = Utils.isSuspendPoint(classLoader, owner, name);


        if (suspendPoint) {
            super.visitVarInsn(Opcodes.ALOAD, thisVarIndex);
            super.visitIntInsn(Opcodes.BIPUSH, suspensionNumber);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, "label$S$S", "I");

            saveFrame();
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if (suspendPoint) {
            doReturn();
            super.visitLabel(labels[suspensionNumber - 1]);
            restoreFrame();
            suspensionNumber++;
        }

    }

    private void doReturn() {
        super.visitInsn(Opcodes.ACONST_NULL);
        super.visitInsn(Opcodes.ARETURN);
    }

    private void restoreFrame() {
        for (SuspendInfo.VarToFieldMapping mapping : suspendInfos.get(suspensionNumber).getMappings()) {
            super.visitVarInsn(Opcodes.ALOAD, thisVarIndex);
            super.visitFieldInsn(Opcodes.GETFIELD, myClassJvmName, mapping.field.fieldName, mapping.field.fieldDescriptor);

            int opcode = Opcodes.ASTORE;
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
            }
            super.visitVarInsn(opcode, mapping.varIndex);
        }
    }

    private void saveFrame() {
        for (SuspendInfo.VarToFieldMapping mapping : suspendInfos.get(suspensionNumber).getMappings()) {
            int opcode = Opcodes.ALOAD;
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
            }
            super.visitVarInsn(Opcodes.ALOAD, thisVarIndex);
            super.visitVarInsn(opcode, mapping.varIndex);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, mapping.field.fieldName, mapping.field.fieldDescriptor);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            super.visitVarInsn(Opcodes.ALOAD, thisVarIndex);
            super.visitIntInsn(Opcodes.BIPUSH, methodInfo.getLabelCount() + 1);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, "label$S$S", "I");
            doReturn();
        } else {
            super.visitInsn(opcode);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitLocalVariable("label", "I", null, new Label(), new Label(), labelVarIndex);
        super.visitMaxs(maxStack, maxLocals);
    }
}
