package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.OPP;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author gl20
 * @date 01/01/2024
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t = getOperand().verifyExpr(compiler, localEnv, currentClass);
        if (t.isInt() || t.isFloat()) {
            setType(t);
            return t;
        }else{
            throw new ContextualError("L'opérateur " + getOperatorName() + " doit être appliqué à un type 'int' ou 'float'.", getLocation());
        }
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler){
        Register Rn = gencode.getCurrentRegister();
        getOperand().codeGenInst(compiler);
        compiler.addInstruction(new OPP(Rn, (GPRegister)Rn));
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        getOperand().byteCodeGenInst(compiler,mv);
        if(getOperand().getType().isInt()){
            mv.visitInsn(Opcodes.INEG);
        }
        if(getOperand().getType().isFloat()){
            mv.visitInsn(Opcodes.FNEG);
        }
    }

    @Override
    protected String getOperatorName() {
        return "-";
    }

}
