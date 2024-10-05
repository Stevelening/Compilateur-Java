package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public abstract class AbstractOpExactCmp extends AbstractOpCmp {

    public AbstractOpExactCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // TODO Venir ajouter ici la partie objet
        Type tLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type tRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (tLeft.isBoolean() && tRight.isBoolean()) {
            Type res = compiler.environmentType.BOOLEAN;
            setType(res);
            return res;   
        }
        return super.verifyExpr(compiler, localEnv, currentClass);
    }

}
