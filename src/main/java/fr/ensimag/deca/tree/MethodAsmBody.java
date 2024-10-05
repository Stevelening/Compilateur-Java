package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.InlinePortion;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.Type;

public class MethodAsmBody extends AbstractMethodBody{

    private StringLiteral asmCode;

    public MethodAsmBody(StringLiteral code){
        Validate.notNull(code);

        this.asmCode = code;
    }

    @Override
    public void codeGenMethodBody(DecacCompiler compiler) {
        compiler.add(new InlinePortion(asmCode.getValue()));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // TODO Auto-generated method stub
        s.print("asm(" + asmCode.decompile() + ");");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        asmCode.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        asmCode.iter(f);
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp paramEnv, ClassDefinition currentClass, Type returnType) {
    }
    
}
