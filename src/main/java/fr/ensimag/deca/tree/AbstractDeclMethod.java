package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * AbstractDeclMethod
 */
public abstract class AbstractDeclMethod extends Tree {


    /**
     * Implémente la règle 2.7 de la passe 2
     * @param compiler contient env_types
     * @param superclass la classe parent
     * @throws ContextualError
     */
    protected abstract void verifyDeclMethodSignature(DecacCompiler compiler, 
            ClassDefinition superclass, EnvironmentExp classEnvExp) throws ContextualError;

    /**
     * Implémente la règle 3.11 de la passe 3
     * @param compiler contient env_types
     * @param className la classe dans laquelle se trouve la méthode
     * @param classEnvExp environnement de la classe
     * @throws ContextualError
     */
    protected abstract void verifyDeclMethodBody(DecacCompiler compiler, 
            ClassDefinition className, EnvironmentExp classEnvExp) throws ContextualError;

    protected abstract void codeGenDeclMethod(DecacCompiler compiler, String className);

    public abstract AbstractIdentifier getMethodName();
}