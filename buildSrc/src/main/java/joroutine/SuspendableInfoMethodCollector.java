package joroutine;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

import java.util.*;
import java.util.stream.Collectors;

public class SuspendableInfoMethodCollector extends AnalyzerAdapter {
    @SuppressWarnings("SpellCheckingInspection")
    private static final char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private static String nextVar(int index) {
        if (index > chars.length * chars.length)
            throw new IllegalArgumentException("only chars.length * chars.length variables are supported for now");

        int f = index / chars.length;
        int s = index % chars.length;

        return String.copyValueOf(new char[]{chars[s], chars[f]});
    }

    private int count = 0;
    private List<StackInfo> stackInfos = new ArrayList<>();

    private int maxLocals;
    private ClassLoader classLoader;

    public SuspendableInfoMethodCollector(ClassLoader classLoader, String owner, int access,
                                          String name, String desc) {
        this(classLoader, owner, access, name, desc, new MethodVisitor(Opcodes.ASM7) {
        });
    }

    public SuspendableInfoMethodCollector(ClassLoader classLoader, String owner, int access,
                                          String name, String desc, MethodVisitor methodVisitor) {
        super(Opcodes.ASM7, owner, access, name, desc, methodVisitor);
        this.classLoader = classLoader;
    }

    public List<SuspendInfo> getSuspendInfos() {
        Map<Object, List<Object>> groupedByType = stackInfos.stream()
                .flatMap(stackInfo -> stackInfo.getVariableTypes().stream())
                .collect(Collectors.groupingBy(o -> o));

        Map<Object, List<String>> fieldsByType = new HashMap<>();

        String postfix = "$S";

        int fieldIndex = 0;

        for (Map.Entry<Object, List<Object>> entry : groupedByType.entrySet()) {
            ArrayList<String> names = new ArrayList<>();
            fieldsByType.put(entry.getKey(), names);

            for (Object ignored : entry.getValue()) {
                names.add(nextVar(fieldIndex++) + postfix);
            }
        }

        List<SuspendInfo> suspendInfos = new ArrayList<>();
        for (StackInfo stackInfo : stackInfos) {
            Map<Object, ArrayList<String>> fieldsByTypeLocal = copyFields(fieldsByType);

            List<SuspendInfo.VarToFieldMapping> mappings = new ArrayList<>();
            mappings.add(new SuspendInfo.VarToFieldMapping(2, new SuspendInfo.Field("scope$S", getDescriptor(stackInfo.getVariableTypes().get(2)))));
            for (int i = 3 /*skip `this` variable*/; i < stackInfo.getVariableTypes().size(); i++) {
                Object variableType = stackInfo.getVariableTypes().get(i);

                ArrayList<String> thisTypeField = fieldsByTypeLocal.get(variableType);
                String descriptor = getDescriptor(variableType);
                if (!descriptor.equals("T")) {
                    mappings.add(new SuspendInfo.VarToFieldMapping(i, new SuspendInfo.Field(thisTypeField.remove(0), descriptor)));
                }
            }


            suspendInfos.add(new SuspendInfo(mappings));
        }

        return Collections.unmodifiableList(suspendInfos);
    }


    private String getDescriptor(Object o) {
        if (o instanceof String)
            return "L" + o + ";";

        if (o instanceof Integer) {
            switch ((Integer) o) {
                case 0: //ITEM_TOP
                    return "T";

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

    private Map<Object, ArrayList<String>> copyFields(Map<Object, List<String>> fieldsByType) {
        return fieldsByType.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, objectListEntry -> new ArrayList<>(objectListEntry.getValue())));
    }

    @Override
    public void visitCode() {
        super.visitCode();

        stackInfos.add(new StackInfo(count++, saveLocals()));
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (Utils.isSuspendPoint(classLoader, owner, name)) {
            stackInfos.add(new StackInfo(count++, saveLocals()));
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    private ArrayList<Object> saveLocals() {
//        ArrayList<Object> locals = new ArrayList<>();
//        for (int i = 0; i < this.locals.size(); i++) {
//            Object e = this.locals.get(i);
//            locals.add(e);
//            if (e instanceof Integer) {
//                switch (((Integer) e)) {
//                    case 3:
//                    case 4:
//                        i++;
//
//                }
//            }
//        }
//
//        return locals;
        return new ArrayList<>(locals);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
        this.maxLocals = maxLocals;
    }

    public int getLabelCount() {
        return count - 1;
    }

    public int getMaxLocals() {
        return maxLocals;
    }
}
