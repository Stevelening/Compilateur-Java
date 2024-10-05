package fr.ensimag.deca.tree;

import java.util.Iterator;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl20
 * @date 01/01/2024
 */
public class ListDeclVar extends TreeList<AbstractDeclVar> {

    //TODO: à vérifier
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclVar declVar : getList()){
            declVar.decompile(s);
            s.println();
        }
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the "env_exp_r" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     */    
    void verifyListDeclVariable(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclVar elem : getList()) {
            elem.verifyDeclVar(compiler, localEnv, currentClass);
        }
    }

    public void codeGenListDeclVar(DecacCompiler compiler){
        for (AbstractDeclVar dv : getList()) {
            dv.codeGenDeclVar(compiler);
        }
    }
    public void byteCodeGenListDeclVar(DecacCompiler compiler, TraceClassVisitor cw){
        for (AbstractDeclVar dv : getList()) {
            dv.byteCodeGenDeclVar(compiler, cw);
        }
    }
    public void byteCodeGenListDeclVar(DecacCompiler compiler, MethodVisitor mv){
        for (AbstractDeclVar dv : getList()) {
            dv.byteCodeGenDeclVar(compiler, mv);
        }
    }

}
