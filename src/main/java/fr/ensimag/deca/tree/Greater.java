package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BGT;
import fr.ensimag.ima.pseudocode.instructions.BLE;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class Greater extends AbstractOpIneq {

    public Greater(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return ">";
    }

    protected void branch(DecacCompiler compiler, boolean b, Label E){
        if(b){
            compiler.addInstruction(new BGT(E));
        }
        else{
            compiler.addInstruction(new BLE(E));
        }
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        org.objectweb.asm.Label tru = new org.objectweb.asm.Label();
        org.objectweb.asm.Label end = new org.objectweb.asm.Label();
        getLeftOperand().byteCodeGenInst(compiler, mv);
        getRightOperand().byteCodeGenInst(compiler, mv);
        if(getLeftOperand().getType().isInt()){
            mv.visitJumpInsn(Opcodes.IF_ICMPGT,tru);
        }
        else if(getLeftOperand().getType().isFloat()){
            mv.visitInsn(Opcodes.FCMPG);
            mv.visitJumpInsn(Opcodes.IFGT,tru);
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
