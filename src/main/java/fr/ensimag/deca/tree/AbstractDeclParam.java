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
public abstract class AbstractDeclParam extends Tree{

    protected abstract Type verifyDeclParamSig(DecacCompiler compiler)
            throws ContextualError;
    
    /**
     * Implements non-terminal "decl_param" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to the "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the synthetized attribute
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     */    
    protected abstract void verifyDeclParam(DecacCompiler compiler,
            EnvironmentExp paramEnv)
            throws ContextualError;

    /**
     * Generate assembly code for the parameters declaration.
     * 
     * @param compiler
     */
    protected abstract void codeGenDeclParam(DecacCompiler compiler, int index);
}
