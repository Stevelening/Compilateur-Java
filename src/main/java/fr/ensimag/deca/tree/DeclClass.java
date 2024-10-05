package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.lang.Validate;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl20
 * @date 01/01/2024
 */
public class DeclClass extends AbstractDeclClass {

    private final AbstractIdentifier className;
    private final AbstractIdentifier superclassName;

    private final ListDeclMethod methods;
    private final ListDeclField fields;

    public DeclClass(AbstractIdentifier name, AbstractIdentifier superclass, ListDeclMethod methods, ListDeclField fields){
        Validate.notNull(fields);
        Validate.notNull(methods);
        //Validate.notNull(superclass); // Peut être null, si c'est la classe Object
        Validate.notNull(name);

        this.className = name;
        this.superclassName = superclass;
        this.methods = methods;
        this.fields = fields;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        String text;
        if (!superclassName.decompile().isEmpty()){
            text = " extends " + superclassName.decompile();
        }
        else{
            text = "";
        }
        s.println("class " + className.decompile() + text + " {");
        s.indent();
        s.println(fields.decompile());
        s.println(methods.decompile());
        s.unindent();
        s.println("}");

    }

    //passe 1
    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        TypeDefinition supTypeDef = compiler.environmentType.defOfType(superclassName.getName());
        if (supTypeDef == null){
            throw new ContextualError("la classe " + superclassName.getName().getName() + " n'éxiste pas", getLocation());
        }
        if (supTypeDef.isClass()){
            Symbol name = className.getName();
            ClassDefinition sup = (ClassDefinition)supTypeDef;
            ClassType type = new ClassType(name, getLocation(), sup);
            ClassDefinition def = type.getDefinition();
            if (compiler.environmentType.defOfType(name) != null){
                throw new ContextualError("classe déjà définie", getLocation());
            }
            compiler.environmentType.addClassToEnvType(name, def);

            //Décoration
            superclassName.setDefinition(sup);
            superclassName.setType(sup.getType());
            className.setType(type);
            className.setDefinition(def);
        }else{
            throw new ContextualError( "" + superclassName.getName().getName() + " n'est pas une classe.", getLocation());
        }
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        //EnvironmentExp superEnv = compiler.environmentType.defOfType(superclassName.getName()).getMembers();
        EnvironmentExp paramfieldEnv = className.getClassDefinition().getMembers();
        className.getClassDefinition().setNumberOfFields(superclassName.getClassDefinition().getNumberOfFields());
        className.getClassDefinition().setNumberOfMethods(superclassName.getClassDefinition().getNumberOfMethods());
        fields.verifyListDeclField(compiler, className.getClassDefinition(), paramfieldEnv);
        methods.verifyListDeclMethodSignature(compiler, className.getClassDefinition(), paramfieldEnv);
    }
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        EnvironmentExp localEnv = className.getClassDefinition().getMembers();//compiler.environmentType.defOfType(className.getName()).getMembers();
        fields.verifyListDeclFieldIntitialization(compiler, className.getClassDefinition(), localEnv);
        methods.verifyListDeclMethodBody(compiler, className.getClassDefinition(), localEnv);
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, false);
        superclassName.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, false);
        fields.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iter(f);
        superclassName.iter(f);
        methods.iter(f);
        fields.iter(f);
    }

    @Override
    protected void codeGenVTable(DecacCompiler compiler){
        // Construction du tableau des etiquettes de la classe
        String name = ""; // nom de l'etiquette
        String name1 = ""; // nom de la methode

        ArrayList<String> copy = new ArrayList<>();
        for(AbstractDeclMethod m : methods.getList()){
            copy.add(m.getMethodName().getName().toString());
        }

        for(int i = 0;i < superclassName.getClassDefinition().getMethodsNames().size();i++){
            int pos = contains(superclassName.getClassDefinition().getMethodsNames().get(i), copy);
            if(pos != -1){
                name = "code." + className.getName().toString() + "." + copy.get(pos);
                name1 = copy.get(pos);
                copy.remove(pos);
            }
            else{
                name = superclassName.getClassDefinition().getMethodLabels().get(i).toString();
                name1 = superclassName.getClassDefinition().getMethodsNames().get(i);
            }

            Label label = new Label(name);
            className.getClassDefinition().addMethodLabel(label);
            className.getClassDefinition().addMethodName(name1);
        }

        for(String s : copy){
            name = "code." + className.getName().toString() + "." +s;
            name1 = s;
            Label label = new Label(name);
            className.getClassDefinition().addMethodLabel(label);
            className.getClassDefinition().addMethodName(name1);
        }
        
        // Construciton de la table des methodes
        gencode.increaseStackSize();
        gencode.increaseGlobalVars(); 
        className.getClassDefinition().setVTableAddress(gencode.getGlobalVars());
        compiler.addComment("Code de la table des méthodes de " + className.getName().toString());
        compiler.addInstruction(new LEA(new RegisterOffset(superclassName.getClassDefinition().getVTableAddress(), Register.GB), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(gencode.getGlobalVars(), Register.GB))); // k(GB) = DVal(R0)
        
        for(Label label : className.getClassDefinition().getMethodLabels()){
            gencode.increaseStackSize();
            gencode.increaseGlobalVars(); 
            compiler.addInstruction(new LOAD(new LabelOperand(label), Register.R0));
            compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(gencode.getGlobalVars(), Register.GB)));   
        }
    }

    protected int contains(String chaine, ArrayList<String> copy){
        for(int i = 0;i < copy.size();i++){
            if(chaine.equals(copy.get(i))){
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void codeGenInitClass(DecacCompiler compiler){
        int d1 = 0;
        String name = "init."+className.getName().toString();
        Label label = new Label(name);
        compiler.addComment("Initialisation des champs de "+className.getName().toString());
        compiler.addLabel(label);
        Line tsto = new Line(new TSTO(0));
        compiler.add(tsto);
        if(compiler.getCompilerOptions().getCheckExecErrors()){
            compiler.addInstruction(new BOV(GenerationCode.stack_overflow_error));
        }
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));

        compiler.addComment("Sauvegarde des registres");
        GenerationCode.isForClass = true;
        GenerationCode.dClass = 0;
        int index = gencode.saveRegisters(compiler); // save registers

        for(AbstractDeclField df : fields.getList()){
            // comment savoir qu'il s'agit d'un champ de la classe courante ?
            Boolean isMyField = df.getFieldName().getFieldDefinition().getContainingClass().getType().getName().toString().
            equals(className.getClassDefinition().getType().toString());
            if(isMyField){
                df.codeGenInitFieldZero(compiler);
            }
        }

        // initialisation des champs hérités
        if(superclassName.getClassDefinition().getNumberOfFields() > 0){
            compiler.addComment("Initialisation des champs herités de "+superclassName.getClassDefinition().getType().getName().toString());
            d1 += 3;
            Label lab = new Label("init."+superclassName.getName().toString());
            compiler.addInstruction(new PUSH(Register.R1));
            compiler.addInstruction(new BSR(new LabelOperand(lab)));
            compiler.addInstruction(new SUBSP(1));
        }

        for(AbstractDeclField df : fields.getList()){
            Boolean isMyField = df.getFieldName().getFieldDefinition().getContainingClass().getType().getName().toString().
            equals(className.getClassDefinition().getType().toString());
            if(isMyField && df.getFieldInitialization() instanceof Initialization){ // initialisation explicite
                df.codeGenInitField(compiler);
            }
        }

        compiler.addComment("Restauration des registres");
        gencode.restoreRegisters(compiler, index); // restore registers
        GenerationCode.isForClass = false;
        d1 += GenerationCode.dClass;

        tsto.setInstruction(new TSTO(d1));
        compiler.addInstruction(new RTS());
    }

    @Override
    protected void codeGenMethods(DecacCompiler compiler){
        methods.codeGenListDeclMethod(compiler, className.getName().toString());
    }

    @Override
    protected void codeGenListDeclField(DecacCompiler compiler){
        fields.codeGenListDeclField(compiler);
    }
}
