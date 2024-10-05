package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.QUO;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type tLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type tRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!tLeft.isInt()) {
            throw new ContextualError("L'opérande gauche de " +getOperatorName()+" doit être de type 'int'.", getLocation());
        }
        if (!tRight.isInt()){
            throw new ContextualError("L'opérande droite de " +getOperatorName()+" doit être de type 'int'.", getLocation());
        }

        Type res = typeBinaryOp(tLeft, tRight);
        setType(res);
        return res;
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected Type typeBinaryOp(Type t1, Type t2) {
        Validate.isTrue(t1.isInt());
        Validate.isTrue(t2.isInt());

        return t1;
    }


    @Override
    protected String getOperatorName() {
        return "%";
    }

    protected void addInstructionOp(DecacCompiler compiler, DVal op1, GPRegister op2){
        compiler.addInstruction(new QUO(op1, op2));
    }
    @Override
    protected int getOpCodeInt() {
        return Opcodes.IREM;
    }

    @Override
    protected int getOpCodeFloat() {
        return Opcodes.FREM;
    }
}
