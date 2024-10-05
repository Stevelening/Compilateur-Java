package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public class Return extends AbstractInst{

    private AbstractExpr operand;

    public Return(AbstractExpr operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }

    public AbstractExpr getOperand() {
        return operand;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType) throws ContextualError{
            if (returnType.isVoid()){
                throw new ContextualError("On ne peut pas retourner un type void", getLocation());
            }
            operand.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler){
        Register Rn = gencode.getCurrentRegister();
        getOperand().codeGenInst(compiler);
        compiler.addInstruction(new LOAD(Rn, Register.R0));
        Label label = new Label(GenerationCode.finMethode);
        compiler.addInstruction(new BRA(label));
    }

    @Override
    public void decompile(IndentPrintStream s){
        s.print("return " + operand.decompile() + ";");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix){
        operand.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f){
        operand.iter(f);
    }
}
