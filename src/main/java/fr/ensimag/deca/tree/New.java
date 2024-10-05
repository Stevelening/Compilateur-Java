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
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;

public class New extends AbstractExpr{

    public AbstractIdentifier getOperand() {
        return operand;
    }

    private AbstractIdentifier operand;
    public New(AbstractIdentifier operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }


    protected String getOperatorName(){
        return "New";
    }
  
    @Override
    public void decompile(IndentPrintStream s) {
         s.print(getOperatorName() + " " + operand.decompile() + "()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        operand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        operand.prettyPrint(s, prefix, true);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError{

        Type res = getOperand().verifyType(compiler);
        if(res.isClass()){
            setType(res);
            return res;
        }
        else{
            throw new ContextualError("L'operateur "+getOperatorName()+" doit etre appliquée a une classe.", getLocation());
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler){
        Register Rn = gencode.getCurrentRegister();
        int d = getOperand().getClassDefinition().getNumberOfFields() + 1;
        compiler.addInstruction(new NEW(new ImmediateInteger(d), (GPRegister)Rn));
        if(compiler.getCompilerOptions().getCheckExecErrors()){
            compiler.addInstruction(new BOV(GenerationCode.heap_error));
        }
        // adresse de la table des methode :
        DAddr daddr = new RegisterOffset(getOperand().getClassDefinition().getVTableAddress(), Register.GB);
        compiler.addInstruction(new LEA(daddr, Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(0, Rn)));
        compiler.addInstruction(new PUSH(Rn));
        if(GenerationCode.isForClass){
            GenerationCode.dClass += 1;
        }
        else if(GenerationCode.isForMethods){
            GenerationCode.dMethod += 1;
        }
        else{
            gencode.increaseStackSize();
        }
        String name = "init."+getOperand().getName().toString();
        compiler.addInstruction(new BSR(new Label(name)));
        compiler.addInstruction(new POP((GPRegister)Rn));
    }
    
}
