package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.DVal;

/**
 *
 * @author Ensimag
 * @date 01/01/2024
 */
public class StringType extends Type {

    public StringType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        return otherType instanceof StringType;
    }


}
