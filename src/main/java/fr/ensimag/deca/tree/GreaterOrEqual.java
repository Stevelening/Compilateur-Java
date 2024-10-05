package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BGE;
import fr.ensimag.ima.pseudocode.instructions.BLT;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Operator "x >= y"
 * 
 * @author gl20
 * @date 01/01/2024
 */
public class GreaterOrEqual extends AbstractOpIneq {

    public GreaterOrEqual(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return ">=";
    }

    protected void branch(DecacCompiler compiler, boolean b, Label E){
        if(b){
            compiler.addInstruction(new BGE(E));
        }
        else{
            compiler.addInstruction(new BLT(E));
        }
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        getLeftOperand().byteCodeGenInst(compiler, mv);
        getRightOperand().byteCodeGenInst(compiler, mv);
        org.objectweb.asm.Label tru = new org.objectweb.asm.Label();
        org.objectweb.asm.Label end = new org.objectweb.asm.Label();
        if(getLeftOperand().getType().isInt()){
            mv.visitJumpInsn(Opcodes.IF_ICMPGE,tru);
        }
        else if(getLeftOperand().getType().isFloat()){
            mv.visitInsn(Opcodes.FCMPG);
            mv.visitJumpInsn(Opcodes.IFGE,tru);
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
}
