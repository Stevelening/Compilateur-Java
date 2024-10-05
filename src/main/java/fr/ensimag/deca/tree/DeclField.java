package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.log4j.Logger;

public class DeclField extends AbstractDeclField{
    private static final Logger LOG = Logger.getLogger(Program.class);
    private final Visibility visibility;
    private final AbstractIdentifier type;
    private final AbstractIdentifier name;
    private final AbstractInitialization initialization;

    public DeclField(Visibility visibility, AbstractIdentifier type, AbstractIdentifier name, AbstractInitialization initialization){
        Validate.notNull(initialization);
        Validate.notNull(type);
        Validate.notNull(name);
        Validate.notNull(visibility);

        this.visibility = visibility;
        this.type = type;
        this.name = name;
        this.initialization = initialization;
    }

    @Override
    public AbstractIdentifier getFieldName(){
        return name;
    }

    @Override
    public AbstractInitialization getFieldInitialization(){
        return initialization;
    }

    @Override
    public AbstractIdentifier getFieldType(){
        return type;
    }

    @Override
    public Visibility getFieldVisibility(){
        return visibility;
    }
    
    /**
     * Règle 2.5 decl_field de la passe 2
     * 
     * @param compiler
     * @param currentClass
     * @param superclass
     * @throws ContextualError
     * 
     * Needs to mutate env_exp
     */
    @Override
    protected void verifyDeclField(DecacCompiler compiler,
            ClassDefinition currentClass, EnvironmentExp fieldEnv)
            throws ContextualError{
        Type synthType = type.verifyType(compiler);
        if (synthType.isVoid() || synthType == null){
            throw new ContextualError("Déclaration de " + name.getName().getName() + " invalide (type void)", getLocation());
        }
        EnvironmentExp superEnv = currentClass.getSuperClass().getMembers();
        if (superEnv.get(name.getName())!=null){
            if (!superEnv.get(name.getName()).isField()){
                throw new ContextualError("" + name.getName().getName() + " Override un Identifiant qui n'est pas un champ", getLocation());
            }
        }
        currentClass.incNumberOfFields();
        int index = currentClass.getNumberOfFields();
        LOG.debug("" + name.getName().getName() + " : " + index);
        ExpDefinition def = new FieldDefinition(synthType, getLocation(), visibility, currentClass, index);
        try {
            fieldEnv.declare(name.getName(), def); // Mutation de l'environnement.    
        } catch (DoubleDefException e) {
            throw new ContextualError("" + name.getName().getName() + " déjà déclaré dans le contexte courant.", getLocation());
        }
        name.setType(synthType);
        name.setDefinition(def);
    }

    /**
     * Règle 3.7 decl_field de la passe 3
     * 
     * @param compiler
     * @param currentClass
     * @param localEnv
     * @throws ContextualError
     * 
     */
    @Override
    protected void verifyDeclFieldInitialization(DecacCompiler compiler,
            ClassDefinition currentClass, EnvironmentExp localEnv)
            throws ContextualError{
        Type synthType = type.verifyType(compiler);

        initialization.verifyInitialization(compiler, synthType, localEnv, currentClass);
    }

    @Override
    protected void codeGenDeclField(DecacCompiler compiler){
        // on lui associe une adresse ici
    }

    @Override
    protected void codeGenInitField(DecacCompiler compiler){
        Register Rn = gencode.getCurrentRegister();
        int recup = gencode.getCurrent();
        compiler.addComment("Initialisation explicite de "+getFieldName().getName().toString());
        ((Initialization)(getFieldInitialization())).getExpression().codeGenInst(compiler);
        // result is in Rn
        compiler.addInstruction(new LOAD(Rn, Register.R0));
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(getFieldName().getFieldDefinition().getIndex(), Register.R1)));
        gencode.setCurrent(recup);
    }

    @Override
    protected void codeGenInitFieldZero(DecacCompiler compiler){
        compiler.addComment("Initialisation a 0 de "+getFieldName().getName().toString());
        if(getFieldType().getType().isInt()){
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0));
        }
        else if(getFieldType().getType().isFloat()){
            compiler.addInstruction(new LOAD(new ImmediateFloat(0), Register.R0));
        }
        else if(getFieldType().getType().isBoolean()){
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0));
        }
        else if(getFieldType().getType().isClass()){
            compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        }
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(getFieldName().getFieldDefinition().getIndex(), Register.R1)));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        String text;
        if (!initialization.decompile().isEmpty()){
            text = " = " + initialization.decompile();
        }
        else{
            text = "";
        }
        String text2;
        if (visibility == Visibility.PROTECTED){
            text2 = visibility.toString().toLowerCase();
        }
        else{
            text2 = "";
        }
        s.print(text2 + " " + type.decompile()  + " " + name.decompile() + text + ";");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        //TODO afficher la visibilité
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);

    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
        initialization.iter(f);
    }
}
