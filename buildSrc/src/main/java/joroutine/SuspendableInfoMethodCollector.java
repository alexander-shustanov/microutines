package joroutine;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuspendableInfoMethodCollector extends MethodVisitor {
    private int count = 0;
    private Map<Integer, LocalVariable> variables = new HashMap<>();
    private int maxLocals;
    private ClassLoader classLoader;

    public SuspendableInfoMethodCollector(ClassLoader classLoader) {
        super(Opcodes.ASM7);
        this.classLoader = classLoader;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
        variables.put(index, new LocalVariable(name, index, descriptor));
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);

        for (int i = 0; i < local.length; i++) {
            if (!variables.containsKey(i) && local[i] != null && local[i] != Opcodes.TOP) {
                String descriptor = createDescriptor(local[i]);
                if (descriptor != null)
                    variables.put(i, new LocalVariable("unnamed", i, descriptor));
            }
        }
    }

    private String createDescriptor(Object o) {
        if (o instanceof String)
            return "L" + o + ";";

        if (o instanceof Integer) {
            switch ((Integer) o) {

                case 1: //ITEM_INTEGER
                    return "I";
                case 2: //ITEM_FLOAT
                    return "F";
                case 3://ITEM_DOUBLE
                    return "D";
                case 4: //ITEM_LONG
                    return "J";
            }
        }
        return null;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (Utils.isSuspendPoint(classLoader, owner, name))
            count++;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
        this.maxLocals = maxLocals;
    }

    public int getCount() {
        return count;
    }

    public List<LocalVariable> getVariables() {
        return new ArrayList<>(variables.values());
    }

    public int getMaxLocals() {
        return maxLocals;
    }
}
