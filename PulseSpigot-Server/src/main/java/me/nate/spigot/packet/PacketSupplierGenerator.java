package me.nate.spigot.packet;

import com.destroystokyo.paper.event.executor.asm.ClassDefiner;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class PacketSupplierGenerator {

    public static <T> Supplier<T> generate(Class<T> packetType) throws InstantiationException, IllegalAccessException {
        String name = generateName();

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor methodVisitor;
        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, name.replace('.', '/'), "Ljava/lang/Object;Ljava/util/function/Supplier<" + Type.getDescriptor(packetType) + ">;", "java/lang/Object", new String[]{"java/util/function/Supplier"});
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(7, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "L" + name.replace('.', '/') + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "get", "()" + Type.getDescriptor(packetType), null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(10, label0);
            methodVisitor.visitTypeInsn(NEW, Type.getInternalName(packetType));
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(packetType), "<init>", "()V", false);
            methodVisitor.visitInsn(ARETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "L" + name.replace('.', '/') + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(2, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC, "get", "()Ljava/lang/Object;", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(7, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, name.replace('.', '/'), "get", "()" + Type.getDescriptor(packetType), false);
            methodVisitor.visitInsn(ARETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "L" + name.replace('.', '/') + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();
        final byte[] bytes = classWriter.toByteArray();

        ClassDefiner definer = ClassDefiner.getInstance();
        return (Supplier<T>) definer.defineClass(PacketSupplierGenerator.class.getClassLoader(), name, bytes).asSubclass(Supplier.class).newInstance();
    }

    public static AtomicInteger NEXT_ID = new AtomicInteger(1);

    public static @NotNull String generateName() {
        int id = NEXT_ID.getAndIncrement();
        return "me.nate.spigot.asm.generated.PacketConstructor" + id;
    }

}