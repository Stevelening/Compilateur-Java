  package fr.ensimag.deca.tree;

import java.util.Iterator;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl20
 * @date 01/01/2024
 */
public class ListDeclField extends TreeList<AbstractDeclField> {

    //TODO: à vérifier
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclField declField : getList()){
            declField.decompile(s);
            s.println();
        }
    }

    //Passe 2    
    void verifyListDeclField(DecacCompiler compiler, ClassDefinition currentClass, EnvironmentExp fieldEnv) 
            throws ContextualError {
        for (AbstractDeclField elem : getList()) {
            elem.verifyDeclField(compiler, currentClass, fieldEnv);
        }
    }
    
    //passe 3
    void verifyListDeclFieldIntitialization(DecacCompiler compiler,
            ClassDefinition currentClass, EnvironmentExp fieldEnv)
            throws ContextualError{
        for (AbstractDeclField elem : getList()) {
            elem.verifyDeclFieldInitialization(compiler, currentClass, fieldEnv);
        }
    }

    public void codeGenListDeclField(DecacCompiler compiler){
        for (AbstractDeclField df : getList()) {
            df.codeGenDeclField(compiler);
        }
    }

}
