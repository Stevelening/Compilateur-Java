package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import org.objectweb.asm.Opcodes;

/**
 * @author gl20
 * @date 01/01/2024
 */
public class Multiply extends AbstractOpArith {
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "*";
    }

    protected void addInstructionOp(DecacCompiler compiler, DVal op1, GPRegister op2){
        compiler.addInstruction(new MUL(op1, op2));
    }
    @Override
    protected int getOpCodeInt() {
        return Opcodes.IMUL;
    }

    @Override
    protected int getOpCodeFloat() {
        return Opcodes.FMUL;
    }

}
