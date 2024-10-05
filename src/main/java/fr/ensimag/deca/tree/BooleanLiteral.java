package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import org.objectweb.asm.MethodVisitor;

import java.io.PrintStream;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class BooleanLiteral extends AbstractExpr {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        if(value == true){// true = #1
            compiler.addInstruction(new LOAD(1, (GPRegister)gencode.getCurrentRegister()));
        }
        else{// false = #0
            compiler.addInstruction(new LOAD(0, (GPRegister)gencode.getCurrentRegister()));
        }
        //compiler.addInstruction(new LOAD((value == true) ? 1 : 0, (GPRegister)gencode.getCurrentRegister()));
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {

        if(this.value == true){
            mv.visitLdcInsn(1);
        }
        else{
            mv.visitLdcInsn(0);
        }
    }

    @Override
    protected void codeGenCond(DecacCompiler compiler, boolean b, Label E){
        if(b){
            if(value){
                compiler.addInstruction(new BRA(E));
            }
            else{
                // nothing
            }
        }
        else{
            if(value){
                // nothing
            }
            else{
                compiler.addInstruction(new BRA(E));
            }
        }
    }
    @Override
    protected void byteCodeGenCond(DecacCompiler compiler, MethodVisitor mv){
        mv.visitLdcInsn(this.value);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Boolean.toString(value));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    String prettyPrintNode() {
        return "BooleanLiteral (" + value + ")";
    }

}
