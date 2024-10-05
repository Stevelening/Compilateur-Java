package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ObjectDeclClass extends AbstractDeclClass{

    public ObjectDeclClass(){
        super();
        setLocation(Location.BUILTIN);
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verifyClass'");
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler) throws ContextualError {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verifyClassMembers'");
    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verifyClassBody'");
    }

    @Override
    public void decompile(IndentPrintStream s) {
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'prettyPrintChildren'");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterChildren'");
    }

    @Override
    protected void codeGenVTable(DecacCompiler compiler){
        // TODO
    }

    @Override
    protected void codeGenInitClass(DecacCompiler compiler){
        // TODO
    }

    @Override
    protected void codeGenMethods(DecacCompiler compiler){
        // TODO
    }

    @Override
    protected void codeGenListDeclField(DecacCompiler compiler){
        // nothing
    }
}
