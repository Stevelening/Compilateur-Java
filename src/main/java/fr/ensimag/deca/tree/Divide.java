package fr.ensimag.deca.tree;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.QUO;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "/";
    }

    protected void addInstructionOp(DecacCompiler compiler, DVal op1, GPRegister op2){
        if(getType().isInt()){
            compiler.addInstruction(new QUO(op1, op2));
        }
        else{
            compiler.addInstruction(new DIV(op1, op2));
        }
    }
    @Override
    protected int getOpCodeInt() {
        return Opcodes.IDIV;
    }

    @Override
    protected int getOpCodeFloat() {
        return Opcodes.FDIV;
    }
}
