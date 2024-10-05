package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.GenerationCode;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.deca.bytecodegen.TypeMap;

import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author gl20
 * @date 01/01/2024
 */
public class DeclVar extends AbstractDeclVar {

    
    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;
    final private TypeMap typeMap = new TypeMap();

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    //Passe 3 :
    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
                Type synthType = type.verifyType(compiler);
                if (synthType.isVoid() || synthType == null){
                    throw new ContextualError("Impossible de déclarer une variable de type void.", getLocation());
                }

                initialization.verifyInitialization(compiler, synthType, localEnv, currentClass);

                
                ExpDefinition def = new VariableDefinition(synthType, getLocation());
                try {
                    localEnv.declare(varName.getName(), def); // Mutation de l'environnement.    
                } catch (DoubleDefException e) {
                    throw new ContextualError("Identifiant déjà déclaré dans le contexte courant.", getLocation());
                }
                
                //Décoration
                varName.setDefinition(def);
                varName.setType(synthType); // À voir s'il vaut mieux faire ça ou override getType dans Identifier pour renvoyer def.getType()
    }

    @Override
    protected void codeGenDeclVar(DecacCompiler compiler){
        if(GenerationCode.isForMethods){
            gencode.increaseLocalVars();
            GenerationCode.dMethod += 1;
        }
        else{
            gencode.increaseStackSize(); // d1 = d1 + 1
            gencode.increaseGlobalVars(); // d2 = d2 + 1
        }
        
        varName.getExpDefinition().setOperand(new RegisterOffset(gencode.getGlobalVars(), Register.GB)); // k(GB)

        initialization.codeGenInitialization(compiler, varName);
    }
    @Override
    protected void byteCodeGenDeclVar(DecacCompiler compiler, TraceClassVisitor cw){
        //TODO : à compléter cas variable classe
        typeMap.initTypeDescriptorMap();
        Map<String,String> typeDescriptorMap = typeMap.getTypeDescriptorMap();
        cw.visitField(ACC_PUBLIC + ACC_STATIC,varName.getName().getName(),typeDescriptorMap.get(type.getName().toString()),null,null).visitEnd();
    }

    @Override
    protected void byteCodeGenDeclVar(DecacCompiler compiler, MethodVisitor mv) {
        typeMap.initTypeDescriptorMap();
        Map<String,String> typeDescriptorMap = typeMap.getTypeDescriptorMap();
        mv.visitCode();
        //TODO : Modifier pour éviter d'utiliser Decompile
        if(type.getType().isFloat() && !initialization.decompile().isEmpty()){
            ((Initialization) initialization).getExpression().byteCodeGenInst(compiler, mv);
            mv.visitFieldInsn(PUTSTATIC,"Main",varName.getName().getName(),"F");
        }
        else if(type.getType().isInt() && !initialization.decompile().isEmpty()){
            ((Initialization) initialization).getExpression().byteCodeGenInst(compiler, mv);
            mv.visitFieldInsn(PUTSTATIC,"Main",varName.getName().getName(),"I");
        }
        else if(type.getType().isBoolean() && !initialization.decompile().isEmpty()){
            ((Initialization) initialization).getExpression().byteCodeGenInst(compiler, mv);
            mv.visitFieldInsn(PUTSTATIC,"Main",varName.getName().getName(),"Z");
        }

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
        s.println(type.decompile() + " " + varName.decompile() + text + ";");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }
}
