package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BGE;
import fr.ensimag.ima.pseudocode.instructions.BLT;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class Lower extends AbstractOpIneq {

    public Lower(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "<";
    }

    protected void branch(DecacCompiler compiler, boolean b, Label E){
        if(b){
            compiler.addInstruction(new BLT(E));
        }
        else{
            compiler.addInstruction(new BGE(E));
        }
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        getLeftOperand().byteCodeGenInst(compiler, mv);
        getRightOperand().byteCodeGenInst(compiler, mv);
        org.objectweb.asm.Label tru = new org.objectweb.asm.Label();
        org.objectweb.asm.Label end = new org.objectweb.asm.Label();
        if(getLeftOperand().getType().isInt()){
            mv.visitJumpInsn(Opcodes.IF_ICMPLT,tru);
        }
        else if(getLeftOperand().getType().isFloat()){
            //Comparaison float push  -1 si plus petit
            mv.visitInsn(Opcodes.FCMPL);
            //vérifie si c'est -1 c'est true on va au label tru sinon, on load 0
            mv.visitJumpInsn(Opcodes.IFLT,tru);
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
