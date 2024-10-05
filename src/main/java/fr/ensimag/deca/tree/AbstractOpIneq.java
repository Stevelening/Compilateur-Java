package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import org.objectweb.asm.MethodVisitor;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public abstract class AbstractOpIneq extends AbstractOpCmp {

    public AbstractOpIneq(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
    @Override
    protected void byteCodeGenCond(DecacCompiler compiler, MethodVisitor mv) {
        //Renvoie 1 sur la pile des opérandes si true
        this.byteCodeGenInst(compiler, mv);
    }

}
