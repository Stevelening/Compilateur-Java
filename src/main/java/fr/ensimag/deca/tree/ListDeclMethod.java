package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclMethod extends TreeList<AbstractDeclMethod> {
    /**
     * Implémente la règle 2.6 de la passe 2
     * @param compiler contient env_types
     * @param superclass la classe parent
     * @throws ContextualError
     */
    protected void verifyListDeclMethodSignature(DecacCompiler compiler, 
            ClassDefinition currentClass, EnvironmentExp methodEnv) throws ContextualError{
        for (AbstractDeclMethod elem : getList()) {
            elem.verifyDeclMethodSignature(compiler, currentClass, methodEnv);
        }
    }

    /**
     * Implémente la règle 3.10 de la passe 3
     * @param compiler contient env_types
     * @param className la classe dans laquelle se trouve la méthode
     * @param classEnvExp environnement de la classe
     * @throws ContextualError
     */
    protected void verifyListDeclMethodBody(DecacCompiler compiler, 
            ClassDefinition currentClass, EnvironmentExp methodEnv) throws ContextualError{
        for (AbstractDeclMethod elem : getList()) {
            elem.verifyDeclMethodBody(compiler, currentClass, methodEnv);
        }
    }

    //TODO: à vérifier
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod declMethod : getList()){
            declMethod.decompile(s);
            s.println();
        }
    }

    public void codeGenListDeclMethod(DecacCompiler compiler, String className){
        for (AbstractDeclMethod dm : getList()) {
            dm.codeGenDeclMethod(compiler, className);
        }
    }

}
