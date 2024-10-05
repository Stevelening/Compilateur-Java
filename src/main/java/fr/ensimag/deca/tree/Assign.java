package fr.ensimag.deca.tree;

import fr.ensimag.deca.bytecodegen.TypeMap;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import javax.management.OperationsException;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import fr.ensimag.deca.context.FieldDefinition;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl20
 * @date 01/01/2024
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue)super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        
        // Il peut arriver que l'on remonte ici l'expression précédée d'un ConvFloat, donc on change l'opérande droite (Arbre Enrichi)
        setRightOperand(getRightOperand().verifyRValue(compiler, localEnv, currentClass, t));

        setType(t);
        return t;
        //throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        int recup = gencode.getCurrent();

        AbstractLValue left = getLeftOperand();
        if(left instanceof Identifier){
            if(left.getExpDefinition().isField()){
                new Assign(new Selection(new This(), (Identifier)left), getRightOperand()).codeGenInst(compiler);;
            }
            else{
                Register Rn = gencode.getCurrentRegister();
                getRightOperand().codeGenInst(compiler);
                DAddr symb = left.getExpDefinition().getOperand();
                compiler.addInstruction(new STORE(Rn, symb));
            } 
        }
        else if(left instanceof Selection){
            Register Rn = gencode.getCurrentRegister();
            GenerationCode.isLeftAssign = true;
            getLeftOperand().codeGenInst(compiler);
            GenerationCode.isLeftAssign = false;
            Boolean hasNext = gencode.nextRegister();
            if(!hasNext){
                compiler.addInstruction(new PUSH(Rn));
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
            getRightOperand().codeGenInst(compiler);

            int k = ((FieldDefinition)(((Selection)left).getExpDefinition())).getIndex();
            if(hasNext){
                compiler.addInstruction(new STORE(Rn1, new RegisterOffset(k, Rn)));
            }
            else{
                GPRegister R0 = Register.R0;
                compiler.addInstruction(new POP(R0));
                compiler.addInstruction(new STORE(Rn1, new RegisterOffset(k, R0)));
            }
        }

        gencode.setCurrent(recup);
    }

    @Override
    protected void codeGenCond(DecacCompiler compiler, boolean b, Label E) {
        Register Rn = gencode.getCurrentRegister();
        codeGenInst(compiler); //Résultat dans Rn

        compiler.addInstruction(new CMP(1, (GPRegister)Rn));

        if(b){
            compiler.addInstruction(new BEQ(E));
        }else{
            compiler.addInstruction(new BNE(E));
        }

    }
    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        Identifier  Ident = (Identifier) getLeftOperand();
        //mv.visitFieldInsn(Opcodes.GETSTATIC, "Main",Ident.getName().getName(), TypeMap.getTypeDescriptor(Ident.getType()));
        getRightOperand().byteCodeGenInst(compiler, mv);
        if(getLeftOperand() instanceof AbstractIdentifier) {
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "Main",Ident.getName().getName(), TypeMap.getTypeDescriptor(Ident.getType()));
        }
    }
    @Override
    public void decompile(IndentPrintStream s){
        getLeftOperand().decompile(s);
        s.print(" = ");
        getRightOperand().decompile(s);
    }
}
