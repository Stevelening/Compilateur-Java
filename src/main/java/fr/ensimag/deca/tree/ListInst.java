package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * 
 * @author gl20
 * @date 01/01/2024
 */
public class ListInst extends TreeList<AbstractInst> {

    /**
     * Implements non-terminal "list_inst" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv corresponds to "env_exp" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     * @param returnType
     *          corresponds to "return" attribute (void in the main bloc).
     */    
    public void verifyListInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        for (AbstractInst i : getList()) {
            i.verifyInst(compiler, localEnv, currentClass, returnType);
        }
        //throw new UnsupportedOperationException("not yet implemented");
    }

    public void codeGenListInst(DecacCompiler compiler) {
        for (AbstractInst i : getList()) {
            i.codeGenInst(compiler);
        }
    }
    public void byteCodeGenListInst(DecacCompiler compiler, MethodVisitor mv) {
        for (AbstractInst i : getList()) {
            i.byteCodeGenInst(compiler, mv);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {

        for (AbstractInst i : getList()) {
            i.decompileInst(s);
            s.println();
        }
    }
}
