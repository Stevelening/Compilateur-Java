package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RFLOAT;
import fr.ensimag.ima.pseudocode.instructions.RINT;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;

import java.io.PrintStream;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class ReadFloat extends AbstractReadExpr {

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        setType(compiler.environmentType.FLOAT);
        return compiler.environmentType.FLOAT;
        //throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readFloat()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        compiler.addInstruction(new RFLOAT()); // result in R1
        if(compiler.getCompilerOptions().getCheckExecErrors()){
            compiler.addInstruction(new BOV(GenerationCode.io_error));
        }
        GPRegister R1 = Register.R1;
        Register Rn = gencode.getCurrentRegister();
        compiler.addInstruction(new LOAD(R1, (GPRegister)Rn)); // Rn = R1
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        mv.visitTypeInsn(Opcodes.NEW,"java/util/Scanner");
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(Opcodes.GETSTATIC,"java/lang/System","in","Ljava/io/InputStream;");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/util/Scanner","<init>","(Ljava/io/InputStream;)V",false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"java/util/Scanner","nextFloat","()F",false);
    }
}
