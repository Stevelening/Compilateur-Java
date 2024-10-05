package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        getOperand().verifyCondition(compiler, localEnv, currentClass);
        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenCond(DecacCompiler compiler, boolean b, Label E){
        getOperand().codeGenCond(compiler, !b, E);
    }
    @Override
    protected void byteCodeGenCond(DecacCompiler compiler, MethodVisitor mv) {
        //Renvoie 1 sur la pile des opérandes si true
        this.byteCodeGenInst(compiler, mv);
    }

    @Override
    protected String getOperatorName() {
        return "!";
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        org.objectweb.asm.Label lbl = new org.objectweb.asm.Label();
        org.objectweb.asm.Label end = new org.objectweb.asm.Label();
        this.getOperand().byteCodeGenCond(compiler, mv);
        mv.visitJumpInsn(Opcodes.IFGT, lbl);
        mv.visitLdcInsn(1);
        mv.visitJumpInsn(Opcodes.GOTO, end);
        mv.visitLabel(lbl);
        mv.visitInsn(Opcodes.F_SAME);
        mv.visitInsn(Opcodes.POP);
        mv.visitLdcInsn(0);
        mv.visitJumpInsn(Opcodes.GOTO, end);
        mv.visitLabel(end);
        mv.visitInsn(Opcodes.F_SAME);
        mv.visitInsn(Opcodes.POP);

    }
}
