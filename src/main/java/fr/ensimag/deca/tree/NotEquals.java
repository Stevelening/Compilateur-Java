package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class NotEquals extends AbstractOpExactCmp {

    public NotEquals(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "!=";
    }

    protected void branch(DecacCompiler compiler, boolean b, Label E){
        if(b){
            compiler.addInstruction(new BNE(E));
        }
        else{
            compiler.addInstruction(new BEQ(E));
        }
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        getLeftOperand().byteCodeGenInst(compiler, mv);
        getRightOperand().byteCodeGenInst(compiler, mv);
        org.objectweb.asm.Label tru = new org.objectweb.asm.Label();
        org.objectweb.asm.Label end = new org.objectweb.asm.Label();
        if(getLeftOperand().getType().isInt()){
            mv.visitJumpInsn(Opcodes.IF_ICMPNE,tru);
        }
        else if(getLeftOperand().getType().isFloat()){
            mv.visitInsn(Opcodes.FCMPL);
            mv.visitJumpInsn(Opcodes.IFNE,tru);
        }
        mv.visitLdcInsn(0);
        mv.visitJumpInsn(Opcodes.GOTO,end);
        mv.visitLabel(tru);
        mv.visitInsn(Opcodes.F_SAME);
        mv.visitInsn(Opcodes.POP);
        mv.visitLdcInsn(1);
        mv.visitJumpInsn(Opcodes.GOTO,end);
        mv.visitLabel(end);
        mv.visitInsn(Opcodes.F_SAME);
        mv.visitInsn(Opcodes.POP);
    }
    @Override
    protected void byteCodeGenCond(DecacCompiler compiler, MethodVisitor mv) {
        //Renvoie 1 sur la pile des opérandes si true
        this.byteCodeGenInst(compiler, mv);
    }
}
