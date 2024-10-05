package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Variable declaration
 *
 * @author gl20
 * @date 01/01/2024
 */
public abstract class AbstractDeclField extends Tree {
    
    /**
     * Règle 2.5 decl_field de la passe 2
     * 
     * @param compiler
     * @param currentClass
     * @param superclass
     * @throws ContextualError
     * 
     * Needs to mutate env_exp
     */   
    protected abstract void verifyDeclField(DecacCompiler compiler,
            ClassDefinition currentClass, EnvironmentExp localEnv)
            throws ContextualError;

    /**
     * Règle 3.7 decl_field de la passe 3
     * 
     * @param compiler
     * @param currentClass
     * @param localEnv
     * @throws ContextualError
     * 
     */
    protected abstract void verifyDeclFieldInitialization(DecacCompiler compiler,
            ClassDefinition currClass, EnvironmentExp localEnv)
            throws ContextualError;

    /**
     * Generate assembly code for the fields declaration.
     * 
     * @param compiler
     */
    protected abstract void codeGenDeclField(DecacCompiler compiler);

    protected abstract void codeGenInitField(DecacCompiler compiler);

    protected abstract void codeGenInitFieldZero(DecacCompiler compiler);

    public abstract AbstractIdentifier getFieldName();

    public abstract AbstractInitialization getFieldInitialization();

    public abstract AbstractIdentifier getFieldType();

    public abstract Visibility getFieldVisibility();
}
