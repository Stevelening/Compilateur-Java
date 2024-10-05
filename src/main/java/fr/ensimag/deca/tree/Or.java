package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class Or extends AbstractOpBool {

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }
    
    @Override
    protected void codeGenCond(DecacCompiler compiler, boolean b, Label E){
        if(b){
            getLeftOperand().codeGenCond(compiler, b, E);
            getRightOperand().codeGenCond(compiler, b, E);
        }
        else{
            gencode.incrIndex();
            Label E_Fin = new Label("E_Fin."+gencode.getIndex());
            getLeftOperand().codeGenCond(compiler, true, E_Fin);
            getRightOperand().codeGenCond(compiler, false, E);
            compiler.addLabel(E_Fin);
        } 
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        getLeftOperand().byteCodeGenInst(compiler, mv);
        getRightOperand().byteCodeGenInst(compiler, mv);
        mv.visitInsn(Opcodes.IOR);
    }
}
