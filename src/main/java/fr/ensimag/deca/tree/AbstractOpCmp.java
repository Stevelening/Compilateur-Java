package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Type tLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type tRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        
        if (!tLeft.isFloat() && !tLeft.isInt()){
            throw new ContextualError("L'opérande gauche de " + getOperatorName() + " doit être de type 'int' ou 'float'.", getLocation());
        }
        if (!tRight.isFloat() && !tRight.isInt()){
            throw new ContextualError("L'opérande droite de " + getOperatorName() + " doit être de type 'int' ou 'float'.", getLocation());
        }

        if (tLeft.isInt() && tRight.isFloat()) {
            setLeftOperand(new ConvFloat(getLeftOperand()));
            getLeftOperand().setType(tRight);
        }
        if (tRight.isInt() && tLeft.isFloat()) {
            setRightOperand(new ConvFloat(getRightOperand()));
            getRightOperand().setType(tLeft);
        }

        Type res = compiler.environmentType.BOOLEAN;
        setType(res);
        return res;
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenCond(DecacCompiler compiler, boolean b, Label E){
        int recup = gencode.getCurrent();

        Register Rn = gencode.getCurrentRegister();
        getLeftOperand().codeGenInst(compiler); // result in Rn

        boolean hasNext = gencode.nextRegister();

        if(!hasNext){
            compiler.addInstruction(new PUSH(Rn)); // save Rn
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

        Register Rn1 = gencode.getCurrentRegister();
        getRightOperand().codeGenInst(compiler); // result in (hasNext) ? Rn+1 : Rn

        if(hasNext){
            compiler.addInstruction(new CMP(Rn1, (GPRegister)Rn));
        }
        else{
            GPRegister R0 = Register.R0;
            compiler.addInstruction(new POP(R0));
            //gencode.decreaseStackSize();
            compiler.addInstruction(new CMP(Rn1, R0));
        }

        branch(compiler, b, E);

        gencode.setCurrent(recup);
    }
    /* Génération de byte code pour les comparaisons : Après une comparaison de type fcomp dans la pile d'opérandes, on a 1 si la v1 > v2, 0 si v1 = v2, -1 sinon
     */
    @Override
    protected void byteCodeGenCond(DecacCompiler compiler, MethodVisitor mv){
        getLeftOperand().byteCodeGenInst(compiler, mv);
        getRightOperand().byteCodeGenInst(compiler, mv);
        org.objectweb.asm.Label tru = new org.objectweb.asm.Label();
        org.objectweb.asm.Label end = new org.objectweb.asm.Label();
        this.byteCodeGenInst(compiler, mv);
        mv.visitJumpInsn(Opcodes.IFGT,tru);
        mv.visitLdcInsn(0);
        mv.visitJumpInsn(Opcodes.GOTO,end);
        mv.visitLabel(tru);
        mv.visitInsn(Opcodes.F_SAME);
        mv.visitLdcInsn(1);
        mv.visitJumpInsn(Opcodes.GOTO,end);
        mv.visitLabel(end);
        mv.visitInsn(Opcodes.F_SAME);
    }
    //Définie dans les sous classes : -push 1 si la condition est vraie
    //                                -push 0 sinon
    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor visitor){
        throw new UnsupportedOperationException("Should never enter here");
    }
    protected abstract void branch(DecacCompiler compiler, boolean b, Label E);
}
