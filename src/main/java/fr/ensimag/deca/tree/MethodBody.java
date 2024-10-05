package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ContextualError;

public class MethodBody extends AbstractMethodBody{

    private ListDeclVar declVars;
    private ListInst insts;

    public MethodBody(ListDeclVar variables, ListInst insts){
        Validate.notNull(variables);
        Validate.notNull(insts);

        this.declVars = variables;
        this.insts = insts;
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp paramEnv, ClassDefinition currentClass, Type returnType) throws ContextualError{
       declVars.verifyListDeclVariable(compiler, paramEnv, currentClass);
       insts.verifyListInst(compiler, paramEnv, currentClass, returnType);
    }

    @Override
    public void codeGenMethodBody(DecacCompiler compiler) {
        declVars.codeGenListDeclVar(compiler);
        insts.codeGenListInst(compiler);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // TODO Auto-generated method stub
        s.println("{");
        s.indent();
        s.println(declVars.decompile());
        s.println(insts.decompile());
        s.unindent();
        s.println("}");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVars.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVars.iter(f);
        insts.iter(f);
    }
    
}
