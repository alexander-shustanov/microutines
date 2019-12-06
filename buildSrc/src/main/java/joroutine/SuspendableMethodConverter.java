package joroutine;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SuspendableMethodConverter extends MethodVisitor {
    private final ClassLoader classLoader;
    private final int numLabels;
    private final Label[] labels;
    private final String myClassJvmName;
    private final int labelVarIndex;
    private final int thisVarOrder;

    private Class currentClass;
    private SuspendableInfoMethodCollector methodInfo;

    int yieldN = 0;

    public SuspendableMethodConverter(MethodVisitor methodVisitor, ClassLoader classLoader, Class currentClass, SuspendableInfoMethodCollector methodInfo) {
        super(Opcodes.ASM7, methodVisitor);
        this.classLoader = classLoader;
        this.currentClass = currentClass;
        this.methodInfo = methodInfo;

        myClassJvmName = currentClass.getName().replace('.', '/');

        this.numLabels = methodInfo.getLabelCount();
        this.labelVarIndex = methodInfo.getMaxLocals();

        thisVarOrder = methodInfo.getVariables().stream().filter(localVariable -> localVariable.name.equals("this")).findFirst().orElseThrow(RuntimeException::new).index;


        labels = new Label[numLabels];
        for (int i = 0; i < numLabels; i++) {
            labels[i] = new Label();
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();

        Label startLabel = new Label();

        super.visitVarInsn(Opcodes.ALOAD, thisVarOrder);
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
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        boolean suspendPoint = Utils.isSuspendPoint(classLoader, owner, name);


        if (suspendPoint) {
            super.visitVarInsn(Opcodes.ALOAD, thisVarOrder);
            super.visitIntInsn(Opcodes.BIPUSH, yieldN + 1);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, "label$S$S", "I");

            saveFrame();
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if (suspendPoint) {
            doReturn();
            super.visitLabel(labels[yieldN]);
            restoreFrame();
            yieldN++;
        }

    }

    private void doReturn() {
        super.visitInsn(Opcodes.ACONST_NULL);
        super.visitInsn(Opcodes.ARETURN);
    }

    private void restoreFrame() {
        methodInfo.getVariables().stream().filter(localVariable -> !localVariable.name.equals("this")).forEach(localVariable -> {
            super.visitVarInsn(Opcodes.ALOAD, thisVarOrder);
            super.visitFieldInsn(Opcodes.GETFIELD, myClassJvmName, localVariable.name + "$S", localVariable.descriptor);

            int opcode = Opcodes.ASTORE;
            switch (localVariable.descriptor) {
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

            super.visitVarInsn(opcode, localVariable.index);
        });
    }

    private void saveFrame() {
        methodInfo.getVariables().stream().filter(localVariable -> !localVariable.name.equals("this")).forEach(localVariable -> {
            int opcode = Opcodes.ALOAD;
            switch (localVariable.descriptor) {
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
            super.visitVarInsn(Opcodes.ALOAD, thisVarOrder);
            super.visitVarInsn(opcode, localVariable.index);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, localVariable.name + "$S", localVariable.descriptor);
        });
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            super.visitVarInsn(Opcodes.ALOAD, thisVarOrder);
            super.visitIntInsn(Opcodes.BIPUSH, methodInfo.getLabelCount() + 1);
            super.visitFieldInsn(Opcodes.PUTFIELD, myClassJvmName, "label$S$S", "I");
            doReturn();
        } else {
            super.visitInsn(opcode);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
//        super.visitLocalVariable("label", "I", null, new Label(), new Label(), labelVarIndex);
        super.visitMaxs(maxStack, maxLocals + 1);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
