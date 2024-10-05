package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;

public abstract class AbstractMethodBody extends Tree{

    // Règle 3.14
    protected abstract void verifyMethodBody(DecacCompiler compiler, EnvironmentExp paramEnv, ClassDefinition currentClass, Type returnType) throws ContextualError;

    public abstract void codeGenMethodBody(DecacCompiler compiler);
}
