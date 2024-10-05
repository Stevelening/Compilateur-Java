package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;


public class TestEnvironmentExp {
    EnvironmentExp testEnv;
    
    
    @BeforeEach
    public void setup(){
        testEnv = new EnvironmentExp(null);
    }

    @Test
    public void testGetInEmpty(){
        SymbolTable table = new SymbolTable();
        Symbol test = table.create("test");
        assertNull(testEnv.get(test));
    }

    @Test
    public void testGetInEmptyWithParent(){
        EnvironmentExp childEnvironmentExp = new EnvironmentExp(testEnv);
        SymbolTable table = new SymbolTable();
        Symbol test = table.create("test");
        assertNull(childEnvironmentExp.get(test));
    }

    @Test
    public void testGetInCurrent(){
        SymbolTable table = new SymbolTable();
        Symbol test = table.create("test");
        ExpDefinition def = new VariableDefinition(null, null);
        try {
            testEnv.declare(test, def);
        } catch (Exception e) {
            System.err.println("fail");
        }
        assertEquals(def, testEnv.get(test));
    }

    @Test
    public void testDoubleDef(){
        SymbolTable table = new SymbolTable();
        Symbol test = table.create("test");
        ExpDefinition def = new VariableDefinition(null, null);
        try {
            testEnv.declare(test, def);
        } catch (Exception e) {
            System.err.println("fail");
            assertTrue(false);
        }
        boolean expectedComp = false;
        try {
            testEnv.declare(test, def);
        } catch (Exception e) {
            assertTrue(true);
            expectedComp = true;
        }
        assertTrue(expectedComp);
    }
}
