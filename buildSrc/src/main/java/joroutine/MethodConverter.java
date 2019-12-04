package joroutine;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodConverter extends MethodVisitor {
    private final Map<String, List<LocalVariable>> varsBySignature;
    private Label[] labels;
    int numLabels;
    int yieldN = 0;
    int frameArgNumber = -1;

    private SuspendableInfoMethodCollector infoCollector;

    public MethodConverter(MethodVisitor methodVisitor, SuspendableInfoMethodCollector infoCollector) {
        super(Opcodes.ASM7, methodVisitor);
        this.infoCollector = infoCollector;
        this.numLabels = infoCollector.getCount();
        labels = new Label[numLabels];
        for (int i = 0; i < numLabels; i++) {
            labels[i] = new Label();
        }

        for (LocalVariable variable : infoCollector.getVariables()) {
            if (variable.name.equals("frame"))
                frameArgNumber = variable.index;
        }

        varsBySignature = infoCollector.getVariables()
                .stream()
                .filter(localVariable -> !localVariable.name.equals("this"))
                .collect(Collectors.groupingBy(localVariable -> {
                    if (localVariable.descriptor.length() == 1)
                        return localVariable.descriptor;
                    else
                        return "Object";
                }));
    }

    @Override
    public void visitCode() {
        super.visitCode();

        for (LocalVariable variable : infoCollector.getVariables()) {
            if (variable.name.equals("this"))
                continue;

            switch (variable.descriptor) {
                case "I":
                    super.visitInsn(Opcodes.ICONST_0);
                    super.visitVarInsn(Opcodes.ISTORE, variable.index);
            }
        }

        int i = 0;
        Label startLabel = new Label();

        super.visitVarInsn(Opcodes.ALOAD, frameArgNumber);
        super.visitFieldInsn(Opcodes.GETFIELD, "StackFrame", "label", "I");
        super.visitIntInsn(Opcodes.BIPUSH, 0);
        super.visitJumpInsn(Opcodes.IF_ICMPLE, startLabel);


        for (; i < numLabels; i++) {
            super.visitVarInsn(Opcodes.ALOAD, frameArgNumber);
            super.visitFieldInsn(Opcodes.GETFIELD, "StackFrame", "label", "I");
            super.visitIntInsn(Opcodes.BIPUSH, i + 1);
            super.visitJumpInsn(Opcodes.IF_ICMPLE, labels[i]);
        }
        super.visitInsn(Opcodes.RETURN);
        super.visitLabel(startLabel);

        initStackFrame();
    }

    private void initStackFrame() {
        for (Map.Entry<String, List<LocalVariable>> entry : varsBySignature.entrySet()) {
            if (entry.getKey().equals("Object")) {

            } else {
                TypeSavingInfo info = TypeSavingInfo.getBYySignature(entry.getKey());
                super.visitVarInsn(Opcodes.ALOAD, frameArgNumber);
                super.visitIntInsn(Opcodes.BIPUSH, entry.getValue().size());
                super.visitIntInsn(Opcodes.NEWARRAY, info.typeOpcode);
                super.visitFieldInsn(Opcodes.PUTFIELD, "StackFrame", info.varName, info.descriptor);

                for (int i = 0; i < entry.getValue().size(); i++) {
                    LocalVariable localVariable = entry.getValue().get(i);
                    super.visitVarInsn(Opcodes.ALOAD, frameArgNumber);
                    super.visitFieldInsn(Opcodes.GETFIELD, "StackFrame", info.varName, info.descriptor);
                    super.visitIntInsn(Opcodes.BIPUSH, i);
                    super.visitInsn(info.aload);
                    super.visitVarInsn(info.store, localVariable.index);

                }
            }
        }
    }

    void initFromFrame() {
        for (Map.Entry<String, List<LocalVariable>> entry : varsBySignature.entrySet()) {
            if (entry.getKey().equals("Object")) {

            } else {
                TypeSavingInfo info = TypeSavingInfo.getBYySignature(entry.getKey());

                for (int i = 0; i < entry.getValue().size(); i++) {
                    LocalVariable localVariable = entry.getValue().get(i);
                    super.visitVarInsn(Opcodes.ALOAD, frameArgNumber);
                    super.visitFieldInsn(Opcodes.GETFIELD, "StackFrame", info.varName, info.descriptor);
                    super.visitIntInsn(Opcodes.BIPUSH, i);
                    super.visitInsn(info.aload);
                    super.visitVarInsn(info.store, localVariable.index);

                }
            }
        }
    }

    void fillFrame() {
        for (Map.Entry<String, List<LocalVariable>> entry : varsBySignature.entrySet()) {
            if (entry.getKey().equals("Object")) {

            } else {
                TypeSavingInfo info = TypeSavingInfo.getBYySignature(entry.getKey());

                for (int i = 0; i < entry.getValue().size(); i++) {
                    LocalVariable localVariable = entry.getValue().get(i);
                    super.visitVarInsn(Opcodes.ALOAD, frameArgNumber);
                    super.visitFieldInsn(Opcodes.GETFIELD, "StackFrame", info.varName, info.descriptor);
                    super.visitIntInsn(info.push, i);
                    super.visitVarInsn(info.load, localVariable.index);
                    super.visitInsn(info.astore);
                }
            }
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if (name.equals("yield")) {
            super.visitVarInsn(Opcodes.ALOAD, frameArgNumber);
            super.visitIntInsn(Opcodes.BIPUSH, yieldN + 1);
            super.visitFieldInsn(Opcodes.PUTFIELD, "StackFrame", "label", "I");

            fillFrame();

            super.visitInsn(Opcodes.RETURN);
            super.visitLabel(labels[yieldN]);

            initFromFrame();

            yieldN++;
        }
    }
}

enum TypeSavingInfo {
    INT("I", Opcodes.T_INT, "[I", "intVars", Opcodes.BIPUSH, Opcodes.ILOAD, Opcodes.IASTORE, Opcodes.IALOAD, Opcodes.ISTORE),
    B("Z", Opcodes.T_BOOLEAN, "[Z", "boolVars", Opcodes.BIPUSH, Opcodes.ILOAD, Opcodes.BASTORE, Opcodes.BALOAD, Opcodes.ISTORE),
    S("S", Opcodes.T_SHORT, "[S", "shortVars", Opcodes.BIPUSH, Opcodes.ILOAD, Opcodes.SASTORE, Opcodes.SALOAD, Opcodes.ISTORE),
    J("J", Opcodes.T_LONG, "[J", "longVars", Opcodes.BIPUSH, Opcodes.LLOAD, Opcodes.LASTORE, Opcodes.LALOAD, Opcodes.LSTORE);

    final String signature;
    final int typeOpcode;
    final String descriptor;
    final String varName;
    final int push;
    final int load;
    final int astore;
    final int aload;
    final int store;

    TypeSavingInfo(String signature, int typeOpcode, String descriptor, String varName, int push, int load, int astore, int aload, int store) {
        this.signature = signature;
        this.typeOpcode = typeOpcode;
        this.descriptor = descriptor;
        this.varName = varName;
        this.push = push;
        this.load = load;
        this.astore = astore;
        this.aload = aload;
        this.store = store;
    }

    static TypeSavingInfo getBYySignature(String signature) {
        for (TypeSavingInfo value : values()) {
            if (value.signature.equals(signature))
                return value;
        }
        System.out.println(signature);
        return null;
    }

}
