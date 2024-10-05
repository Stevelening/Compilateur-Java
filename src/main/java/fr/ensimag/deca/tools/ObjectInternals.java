package fr.ensimag.deca.tools;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tree.AbstractDeclClass;
import fr.ensimag.deca.tree.AbstractDeclMethod;
import fr.ensimag.deca.tree.AbstractIdentifier;
import fr.ensimag.deca.tree.Identifier;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.ObjectDeclClass;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE;

public class ObjectInternals {

    //Declaration classe

    private final static AbstractDeclClass classDeclaration = new ObjectDeclClass();
    public static AbstractDeclClass getClassDeclaration(){
        return classDeclaration;
    }

    //Définition equals

    private static boolean equalsDefined = false;
    public static MethodDefinition initializeObjEqualsDefinition(DecacCompiler compiler){
        if(equalsDefined){
            throw new DecacInternalError("Definition de Object.equals déjà initialisée, il faut la récupérer depuis l'environnement");
        }
        equalsDefined = true;

        Signature sig = new Signature();
        sig.add(compiler.environmentType.OBJECT);

        compiler.environmentType.OBJECT.getDefinition().incNumberOfMethods();
        return new MethodDefinition(compiler.environmentType.BOOLEAN, Location.BUILTIN, sig, 1);
    }

    //objectClass Identifier
    private static AbstractIdentifier objIdent;

    public static AbstractIdentifier getIdentifier(DecacCompiler compiler){
        if (objIdent == null) {
            objIdent = new Identifier(compiler.createSymbol("Object"));
            objIdent.setLocation(Location.BUILTIN);
        }
        return objIdent;
    }

    public static String getMethodName(){
        return "equals";
    }

    public static void codeGenObjectVTable(DecacCompiler compiler){
        // construction du tableau des etiquettes
        String name = "code." + getIdentifier(compiler).getName().toString() + "."+getMethodName();
        Label label = new Label(name);
        getIdentifier(compiler).getClassDefinition().addMethodLabel(label);
        getIdentifier(compiler).getClassDefinition().addMethodName(getMethodName());

        // construction de la table des mehtodes
        GenerationCode gencode = new GenerationCode();
        gencode.increaseStackSize();
        gencode.increaseGlobalVars(); 
        getIdentifier(compiler).getClassDefinition().setVTableAddress(gencode.getGlobalVars());
        compiler.addComment("Code de la table des méthodes de " + getIdentifier(compiler).getName().toString());
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0)); // R0 = #null
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(gencode.getGlobalVars(), Register.GB))); // k(GB) = DVal(R0)
        
        gencode.increaseStackSize();
        gencode.increaseGlobalVars(); 
        compiler.addInstruction(new LOAD(new LabelOperand(label), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(gencode.getGlobalVars(), Register.GB)));
    }

    public static void codeGenInitObject(DecacCompiler compiler){
        // nothing
    }

    public static void codeGenObjectMethods(DecacCompiler compiler){
        compiler.addComment("Code de la méthode "+getMethodName());
        String name = getIdentifier(compiler).getName().toString();
        name += "." + getMethodName();
        compiler.addLabel(new Label("code."+name));
        Label finMehtode = new Label("fin."+name);
        
        // Body de equals
        GenerationCode gencode = new GenerationCode();
        Register Rn = gencode.getCurrentRegister();
        gencode.incrIndex();
        Label notEquals = new Label("not_equals."+gencode.getIndex());
        Label finEquals = new Label("fin_equals."+gencode.getIndex());

        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), (GPRegister)Rn));
        compiler.addInstruction(new CMP(new RegisterOffset(-3, Register.LB), (GPRegister)Rn));
        compiler.addInstruction(new BNE(notEquals));
        compiler.addInstruction(new LOAD(1, Register.R0));
        compiler.addInstruction(new BRA(finEquals));
        compiler.addLabel(notEquals);
        compiler.addInstruction(new LOAD(0, Register.R0));
        compiler.addLabel(finEquals);

        compiler.addLabel(finMehtode);
        compiler.addInstruction(new RTS());
    }
}
