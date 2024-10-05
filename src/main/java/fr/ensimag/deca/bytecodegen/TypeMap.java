package fr.ensimag.deca.bytecodegen;

import fr.ensimag.deca.context.Type;

import java.util.HashMap;
import java.util.Map;

public class TypeMap {
    Map<String,String> typeDescriptorMap = new HashMap<String,String>();
    public void initTypeDescriptorMap(){
        typeDescriptorMap.put("int","I");
        typeDescriptorMap.put("float","F");
        typeDescriptorMap.put("boolean","Z");
    }
    public Map<String,String> getTypeDescriptorMap(){
        return typeDescriptorMap;
    }
    public static String getTypeDescriptor(Type type){
        if(type.isBoolean()){
            return "Z";
        } else if (type.isFloat()) {
            return "F";
        } else if (type.isInt()) {
            return "I";
        } else if (type.isString()) {
            return "Ljava/lang/String;";
        }
            throw new IllegalArgumentException("Impossible de déterminer le type de cette variable");
    }
}
