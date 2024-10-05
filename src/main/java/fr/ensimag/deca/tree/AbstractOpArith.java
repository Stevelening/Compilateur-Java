package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl20
 * @date 01/01/2024
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {


        Type tLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type tRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!tLeft.isFloat() && !tLeft.isInt()){
            throw new ContextualError("L'opérande gauche de " +getOperatorName()+" doit être de type 'int' ou 'float'.", getLocation());
        }
        if (!tRight.isFloat() && !tRight.isInt()) {
            throw new ContextualError("L'opérande droite de " +getOperatorName()+" doit être de type 'int' ou 'float'.", getLocation());
        }

        if (tLeft.isInt() && tRight.isFloat()) {
            setLeftOperand(new ConvFloat(getLeftOperand()));
        }
        if (tRight.isInt() && tLeft.isFloat()) {
            setRightOperand(new ConvFloat(getRightOperand()));
        }

        Type res = typeBinaryOp(tLeft, tRight);
        setType(res);
        return res;
        
        //throw new UnsupportedOperationException("not yet implemented");
    }

    //Par défaut, int si 2 int et float sinon, à réécrire dans modulo et dans divide.
    @Override
    protected Type typeBinaryOp(Type t1, Type t2) {
        Validate.isTrue(t1.isFloat() || t1.isInt());
        Validate.isTrue(t2.isFloat() || t2.isInt());
        return t1.isFloat() ? t1 : t2;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        int recup = gencode.getCurrent();

        Register Rn = gencode.getCurrentRegister(); // Rn
        getLeftOperand().codeGenInst(compiler);

        boolean hasNext = gencode.nextRegister(); // registre courant s'incremente ssi n < 15

        if(!hasNext){
            compiler.addInstruction(new PUSH(Rn)); // on sauvegarde le contenu du registre Rn
            if(GenerationCode.isForClass){
                GenerationCode.dClass += 1;
            }
            else if(GenerationCode.isForMethods){
                GenerationCode.dMethod += 1;
            }
            else{
                gencode.increaseStackSize();
            }
        }

        Register Rn1 = gencode.getCurrentRegister(); // Rn1 = (n < MAX) ? Rn+1 : Rn
        getRightOperand().codeGenInst(compiler);

        if(hasNext){
            addInstructionOp(compiler, Rn1, (GPRegister)Rn);
            if(compiler.getCompilerOptions().getCheckExecErrors()){
                compiler.addInstruction(new BOV(GenerationCode.overflow_error));
            } 
        }
        else{
            GPRegister R0 = Register.R0;
            compiler.addInstruction(new POP(R0));
            //gencode.decreaseStackSize();
            addInstructionOp(compiler, Rn, R0);
            if(compiler.getCompilerOptions().getCheckExecErrors()){
                compiler.addInstruction(new BOV(GenerationCode.overflow_error));
            }
            compiler.addInstruction(new LOAD(R0, (GPRegister)Rn));
        }

        gencode.setCurrent(recup);
    }

    // instruction assembleur correspondant a l'operation
    protected abstract void addInstructionOp(DecacCompiler compiler, DVal op1, GPRegister op2);

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        getLeftOperand().byteCodeGenInst(compiler, mv);
        getRightOperand().byteCodeGenInst(compiler, mv);
        if(getType().isFloat()){
            mv.visitInsn(this.getOpCodeFloat());
        }
        else if (getType().isInt()){
            mv.visitInsn(this.getOpCodeInt());
        }
        else{
            throw new IllegalArgumentException("Opérandes de type incompatible");
        }
    }

    protected abstract int getOpCodeInt();
    protected abstract int getOpCodeFloat();
}
