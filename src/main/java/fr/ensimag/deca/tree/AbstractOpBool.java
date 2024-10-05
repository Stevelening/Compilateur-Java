package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
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
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //TODO peut-être pas bon tout ça (ça a l'air d'être ce qu'on veut quand même)
        getLeftOperand().verifyCondition(compiler, localEnv, currentClass);
        getRightOperand().verifyCondition(compiler, localEnv, currentClass);
        Type res = compiler.environmentType.BOOLEAN;
        setType(res);
        return res;
        //throw new UnsupportedOperationException("not yet implemented");
    }
    @Override
    protected void byteCodeGenCond(DecacCompiler compiler, MethodVisitor mv) {
        //Renvoie 1 sur la pile des opérandes si true
        this.byteCodeGenInst(compiler, mv);
    }

}
