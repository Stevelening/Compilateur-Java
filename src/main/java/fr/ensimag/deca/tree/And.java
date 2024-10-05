package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl20
 * @date 01/01/2024
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // on cree une etiquette
        //Label E = new Label("E");

        // on genere le code pour la partie gauche
        getLeftOperand().codeGenInst(compiler);

        // on ajoute le BEQ vers l'etiquette
        //compiler.addInstruction(new BEQ(E));

        // on genere le code pour la partie droite
        getRightOperand().codeGenInst(compiler);

        // on ajoute l'etiquette E
        //compiler.addLabel(E);
    }

    @Override
    protected void byteCodeGenInst(DecacCompiler compiler, MethodVisitor mv) {
        //Renvoie 1 sur la pile des opérandes si true
        getLeftOperand().byteCodeGenInst(compiler, mv);
        getRightOperand().byteCodeGenInst(compiler, mv);
        mv.visitInsn(Opcodes.IAND);
    }

    private static int index = 0;
    public int getIndex(){
        return index;
    }

    @Override
    protected void codeGenCond(DecacCompiler compiler, boolean b, Label E){
        if(b){
            gencode.incrIndex();
            Label E_Fin = new Label("E_Fin."+gencode.getIndex());
            getLeftOperand().codeGenCond(compiler, false, E_Fin);
            getRightOperand().codeGenCond(compiler, true, E);
            compiler.addLabel(E_Fin);
        }
        else{
            getLeftOperand().codeGenCond(compiler, b, E);
            getRightOperand().codeGenCond(compiler, b, E);
        } 
    }

}
