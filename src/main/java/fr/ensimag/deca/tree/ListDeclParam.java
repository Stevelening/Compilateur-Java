package fr.ensimag.deca.tree;

import java.util.Iterator;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl20
 * @date 01/01/2024
 */
public class ListDeclParam extends TreeList<AbstractDeclParam> {

    //TODO: à vérifier
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclParam declParam : getList()){
            declParam.decompile(s);
            s.println();
        }
    }

    /**
     * Implements non-terminal "list_decl_param" of [SyntaxeContextuelle] in pass 2
     * @param compiler contains the "env_types" attribute
     */    
    Signature verifyListDeclParamSig(DecacCompiler compiler) throws ContextualError {
        Signature sig = new Signature();
        for (AbstractDeclParam elem : getList()) {
           sig.add(elem.verifyDeclParamSig(compiler)); 
        }
        return sig;
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     */    
    void verifyListDeclParam(DecacCompiler compiler, EnvironmentExp paramEnv) throws ContextualError {
        for (AbstractDeclParam elem : getList()) {
            elem.verifyDeclParam(compiler, paramEnv); 
        }
    }

    public void codeGenListDeclParam(DecacCompiler compiler){
        int index = 1;
        for(AbstractDeclParam p : getList()){
            p.codeGenDeclParam(compiler, index);
            index += 1;
        }
    }

}
