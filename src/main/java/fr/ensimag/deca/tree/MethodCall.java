package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;

public class MethodCall extends AbstractExpr{

    AbstractExpr selection;
    AbstractIdentifier methodName;
    ListExpr args;

    public MethodCall(AbstractExpr selection, AbstractIdentifier name, ListExpr args){
        Validate.notNull(name);
        Validate.notNull(args);
        Validate.notNull(selection); 

        this.selection = selection;
        this.methodName = name;
        this.args = args;
    }
    
    //passe 3
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        ClassType type = (ClassType)selection.verifyExpr(compiler, localEnv, currentClass);
        ClassDefinition classDef = type.getDefinition();
        ExpDefinition expDef = classDef.getMembers().get(methodName.getName()); 
        if (expDef == null){
            throw new ContextualError("" + methodName.getName().getName() + " n'éxiste pas", getLocation());
        }
        MethodDefinition def = expDef.asMethodDefinition("" + methodName.getName().getName() + " ne correspond pas à une méthode", getLocation());
        Signature sig = def.getSignature();
        if (sig.size() < args.size()){
            throw new ContextualError("trop d'arguments pour la méthode", getLocation());
        }else if (sig.size() > args.size()){
            throw new ContextualError("pas assez d'arguments pour la méthode", getLocation());
        }
        int i = 0;
        for (AbstractExpr arg : args.getList()){
            arg.verifyRValue(compiler, localEnv, currentClass, sig.paramNumber(i));
            i++;
        }

        //décoration
        methodName.setDefinition(def);
        Type methodType = def.getType();
        setType(methodType);
        
        return methodType;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        String text;
        if (!selection.decompile().isEmpty()){
            text = selection.decompile() + ".";
        }
        else{
            text = "";
        }
        s.print(text + methodName.decompile() + "(" + args.decompile() + ")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        selection.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        args.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        selection.iter(f);
        methodName.iter(f);
        args.iter(f);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler){
        Register Rn = gencode.getCurrentRegister();
        int d = args.size() + 1; // le +1 c'est pour le parametre implicite qui est l'objet sur lequel est appelé la méthode
        gencode.increaseStackSize(d);
        compiler.addInstruction(new ADDSP(new ImmediateInteger(d)));
        selection.codeGenInst(compiler);
        compiler.addInstruction(new STORE(Rn, new RegisterOffset(0, Register.SP)));
        int i = 1;
        for(AbstractExpr expr : args.getList()){
            expr.codeGenInst(compiler);
            compiler.addInstruction(new STORE(Rn, new RegisterOffset(-i, Register.SP)));
            i += 1;
        }
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), (GPRegister)Rn));
        compiler.addInstruction(new CMP(new NullOperand(), (GPRegister)Rn));
        if(compiler.getCompilerOptions().getCheckExecErrors()){
            compiler.addInstruction(new BEQ(GenerationCode.dereferencement_null));
        }
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Rn), (GPRegister)Rn));
        compiler.addInstruction(new BSR(new RegisterOffset(methodName.getMethodDefinition().getIndex(), Rn)));
        compiler.addInstruction(new SUBSP(new ImmediateInteger(d)));
        if(!methodName.getMethodDefinition().getType().isVoid()){
            compiler.addInstruction(new LOAD(Register.R0, (GPRegister)Rn));
        }   
    }

    @Override
    protected void codeGenCond(DecacCompiler compiler, boolean b, Label E){
        Register Rn = gencode.getCurrentRegister();
        codeGenInst(compiler);
        compiler.addInstruction(new CMP(0, (GPRegister)Rn));
        if(b){
            compiler.addInstruction(new BNE(E));
        }
        else{
            compiler.addInstruction(new BEQ(E));
        }
    }
}
