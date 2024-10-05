package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;

import java.io.PrintStream;
import java.util.Iterator;

import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Full if/else if/else statement.
 *
 * @author gl20
 * @date 01/01/2024
 */
public class IfThenElse extends AbstractInst {
    
    private final AbstractExpr condition; 
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        condition.verifyCondition(compiler, localEnv, currentClass);
        Iterator<AbstractInst> it = thenBranch.iterator();
        while (it.hasNext()) {
            AbstractInst inst = it.next();
            inst.verifyInst(compiler, localEnv, currentClass, returnType);
        }

        Iterator<AbstractInst> elseIt = elseBranch.iterator();
        while (elseIt.hasNext()) {
            AbstractInst inst = elseIt.next();
            inst.verifyInst(compiler, localEnv, currentClass, returnType);
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        gencode.incrIndex();
        Label E_Sinon = new Label("E_Sinon."+gencode.getIndex()); 
        Label E_Fin = new Label("E_Fin."+gencode.getIndex());

        condition.codeGenCond(compiler,false, E_Sinon);

        thenBranch.codeGenListInst(compiler);

        compiler.addInstruction(new BRA(E_Fin));

        compiler.addLabel(E_Sinon);

        elseBranch.codeGenListInst(compiler);

        compiler.addLabel(E_Fin);
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        this.condition.byteCodeGenCond(compiler, mv);
        org.objectweb.asm.Label tru = new org.objectweb.asm.Label();
        org.objectweb.asm.Label end = new org.objectweb.asm.Label();
        mv.visitJumpInsn(Opcodes.IFGT,tru);
        elseBranch.byteCodeGenListInst(compiler,mv);
        mv.visitJumpInsn(Opcodes.GOTO,end);
        mv.visitLabel(tru);
        mv.visitInsn(Opcodes.F_SAME);
        mv.visitInsn(Opcodes.POP);
        thenBranch.byteCodeGenListInst(compiler,mv);
        mv.visitJumpInsn(Opcodes.GOTO,end);
        mv.visitLabel(end);
        mv.visitInsn(Opcodes.F_SAME);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.NOP);
    }
    @Override
    public void decompile(IndentPrintStream s) {
        s.println("if (" + this.condition.decompile() + ") { " );
        s.indent();
        this.thenBranch.decompile(s);
        s.unindent();
        s.println("}");
        s.println("else {");
        s.indent();
        elseBranch.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }
}
