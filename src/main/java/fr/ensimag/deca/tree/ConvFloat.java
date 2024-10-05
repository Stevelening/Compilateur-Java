package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;

import java.nio.file.attribute.GroupPrincipal;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.IntType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl20
 * @date 01/01/2024
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t = getOperand().verifyExpr(compiler, localEnv, currentClass);
        if (t.isInt()){
            setType(compiler.environmentType.FLOAT);
            return compiler.environmentType.FLOAT;
        }
        throw new ContextualError("Impossible de convertir l'expression en 'float'.", getLocation());
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        Register Rn = gencode.getCurrentRegister();
        getOperand().codeGenInst(compiler); // result in Rn
        compiler.addInstruction(new FLOAT(Rn, (GPRegister)Rn)); // Rn = DVal de Rn
        if(compiler.getCompilerOptions().getCheckExecErrors()){
            compiler.addInstruction(new BOV(GenerationCode.overflow_error)); // si DVal non codable sur un flottant
        }
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        getOperand().byteCodeGenInst(compiler,mv);
        mv.visitInsn(Opcodes.I2F);
    }

    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

}
