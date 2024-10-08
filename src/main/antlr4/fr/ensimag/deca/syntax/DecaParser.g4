parser grammar DecaParser;

options {
    // Default language but name it anyway
    //
    language  = Java;

    // Use a superclass to implement all helper
    // methods, instance variables and overrides
    // of ANTLR default methods, such as error
    // handling.
    //
    superClass = AbstractDecaParser;

    // Use the vocabulary generated by the accompanying
    // lexer. Maven knows how to work out the relationship
    // between the lexer and parser and will build the
    // lexer before the parser. It will also rebuild the
    // parser if the lexer changes.
    //
    tokenVocab = DecaLexer;

}

// which packages should be imported?
@header {
    import fr.ensimag.deca.tree.*;
    import java.io.PrintStream;
}

@members {
    @Override
    protected AbstractProgram parseProgram() {
        return prog().tree;
    }
}

prog returns[AbstractProgram tree]
    : list_classes main EOF {
            assert($list_classes.tree != null);
            assert($main.tree != null);
            $tree = new Program($list_classes.tree
            , $main.tree);
            setLocation($tree, $list_classes.start);
        }
    ;

main returns[AbstractMain tree]
    : /* epsilon */ {
            $tree = new EmptyMain();
            setLocation($tree, $start);
        }
    | block {
            assert($block.decls != null);
            assert($block.insts != null);
            $tree = new Main($block.decls, $block.insts);

            setLocation($tree, $block.start);
        }
    ;

block returns[ListDeclVar decls, ListInst insts]
    : OBRACE list_decl list_inst CBRACE {
            assert($list_decl.tree != null);
            assert($list_inst.tree != null);
            $decls = $list_decl.tree;
            $insts = $list_inst.tree;
        }
    ;

list_decl returns[ListDeclVar tree]
@init   {
            $tree = new ListDeclVar();
        }

    : decl_var_set[$tree]*
    ;

decl_var_set[ListDeclVar l]
    : type list_decl_var[$l,$type.tree] SEMI
    ;

list_decl_var[ListDeclVar l, AbstractIdentifier t]
    : dv1=decl_var[$t] {
        $l.add($dv1.tree);
        } (COMMA dv2=decl_var[$t] {
        $l.add($dv2.tree);
        }
      )*
    ;

decl_var[AbstractIdentifier t] returns[AbstractDeclVar tree]
@init   {
    AbstractInitialization init;
        }
    : i=ident {
        assert($i.tree != null);
        init = new NoInitialization();
        setLocation(init, $i.start);
        $tree = new DeclVar($t, $i.tree, init);
        setLocation($tree, $start);
        }
      (EQUALS e=expr {
        assert($e.tree != null);
        init = new Initialization($e.tree);
        setLocation(init, $e.start);
        $tree = new DeclVar($t, $i.tree, init);
        setLocation($tree, $start);
        }
      )?
    ;

list_inst returns[ListInst tree]
@init {$tree = new ListInst();}
    : (inst {
      $tree.add($inst.tree);
      }
      )*
    ;

inst returns[AbstractInst tree]

    : 

     SEMI {
           $tree = new NoOperation();
           setLocation($tree, $start);
        }

    | PRINT OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Print(false,$list_expr.tree);
            setLocation($tree, $start);
        }

    | PRINTLN OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Println(false,$list_expr.tree);
            setLocation($tree, $start);
        }

    | PRINTX OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Print(true,$list_expr.tree);
            setLocation($tree, $start);
        }

    | PRINTLNX OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Println(true,$list_expr.tree);
            setLocation($tree, $start);
        }
    | if_then_else {
            assert($if_then_else.tree != null);
            
            $tree = $if_then_else.tree;
        }
    | WHILE OPARENT condition=expr CPARENT OBRACE body=list_inst CBRACE {
            assert($condition.tree != null);
            assert($body.tree != null);

            $tree = new While($condition.tree, $body.tree);
            setLocation($tree, $body.start);
        }
    | RETURN expr SEMI {
            assert($expr.tree != null);

            $tree = new Return($expr.tree);
            setLocation($tree, $start);
        }
    | e1=expr SEMI {
            assert($e1.tree != null);
            $tree = $e1.tree;
        }
    ;

if_then_else returns[IfThenElse tree]
@init {
    ListInst else_branch = new ListInst();
    ListInst last_else;
}
    : if1=IF OPARENT condition=expr CPARENT OBRACE li_if=list_inst CBRACE {
        assert($condition.tree != null);
        assert($li_if.tree != null);

        $tree = new IfThenElse($condition.tree, $li_if.tree, else_branch);
        setLocation($tree, $li_if.start);

        last_else = else_branch;
        }
      (ELSE elsif=IF OPARENT elsif_cond=expr CPARENT OBRACE elsif_li=list_inst CBRACE {
        assert($elsif_cond.tree != null);
        assert($elsif_li.tree != null);

        else_branch = new ListInst();
        IfThenElse ifimb = new IfThenElse($elsif_cond.tree, $elsif_li.tree, else_branch);
        setLocation(ifimb, $elsif_li.start);

        last_else.add(ifimb);
        last_else = else_branch;
        }
      )*
      (ELSE OBRACE li_else=list_inst CBRACE {
        assert($li_else.tree != null);

        for (AbstractInst inst : $li_else.tree.getList()){
            last_else.add(inst);
        }
        }
      )?
    ;

list_expr returns[ListExpr tree]
@init   { $tree = new ListExpr(); }
    : (e1=expr {
        $tree.add($e1.tree);
        }
       (COMMA e2=expr {
        $tree.add($e2.tree);
        }
       )* )?
    ;

expr returns[AbstractExpr tree]
    : e=assign_expr {
            assert($assign_expr.tree != null);
            $tree = $e.tree;
        }
    ;

assign_expr returns[AbstractExpr tree]
    : e=or_expr (
        /* condition: expression e must be a "LVALUE" */ {
            if (! ($e.tree instanceof AbstractLValue)) {
                throw new InvalidLValue(this, $ctx);
            }
            $tree = $e.tree;
        }
        EQUALS e2=assign_expr {
            assert($e.tree != null);
            assert($e2.tree != null);

            $tree = new Assign((AbstractLValue)$e.tree, $e2.tree);
            setLocation($tree, $start);
        }
      | /* epsilon */ {
            assert($e.tree != null);
            $tree = $e.tree;
        }
      )
    ;

or_expr returns[AbstractExpr tree]
    : e=and_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=or_expr OR e2=and_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);

            $tree = new Or($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
       }
    ;

and_expr returns[AbstractExpr tree]
    : e=eq_neq_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    |  e1=and_expr AND e2=eq_neq_expr {
            assert($e1.tree != null);                         
            assert($e2.tree != null);

            $tree = new And($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    ;

eq_neq_expr returns[AbstractExpr tree]
    : e=inequality_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=eq_neq_expr EQEQ e2=inequality_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);

            $tree = new Equals($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    | e1=eq_neq_expr NEQ e2=inequality_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);

            $tree = new NotEquals($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    ;

inequality_expr returns[AbstractExpr tree]
    : e=sum_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=inequality_expr LEQ e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);

            $tree = new LowerOrEqual($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    | e1=inequality_expr GEQ e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);

            $tree = new GreaterOrEqual($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    | e1=inequality_expr GT e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);

            $tree = new Greater($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    | e1=inequality_expr LT e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);

            $tree = new Lower($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    | e1=inequality_expr INSTANCEOF type {
            assert($e1.tree != null);
            assert($type.tree != null);

            //TODO
        }
    ;


sum_expr returns[AbstractExpr tree]
    : e=mult_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=sum_expr PLUS e2=mult_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);

            $tree = new Plus($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    | e1=sum_expr MINUS e2=mult_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);

            $tree = new Minus($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    ;

mult_expr returns[AbstractExpr tree]
    : e=unary_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=mult_expr TIMES e2=unary_expr {
            assert($e1.tree != null);                                         
            assert($e2.tree != null);

            $tree = new Multiply($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    | e1=mult_expr SLASH e2=unary_expr {
            assert($e1.tree != null);                                         
            assert($e2.tree != null);

            $tree = new Divide($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    | e1=mult_expr PERCENT e2=unary_expr {
            assert($e1.tree != null);                                                                          
            assert($e2.tree != null);

            $tree = new Modulo($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
        }
    ;

unary_expr returns[AbstractExpr tree]
    : op=MINUS e=unary_expr {
            assert($e.tree) != null;

            $tree = new UnaryMinus($e.tree);
            setLocation($tree, $start);
        }
    | op=EXCLAM e=unary_expr {
            assert($e.tree != null);

            $tree = new Not($e.tree);
            setLocation($tree, $start);
        }
    | select_expr {
            assert($select_expr.tree != null);
            $tree = $select_expr.tree;
        }
    ;

select_expr returns[AbstractExpr tree]
    : e=primary_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=select_expr DOT i=ident {
            assert($e1.tree != null);
            assert($i.tree != null);
        }
        (o=OPARENT args=list_expr CPARENT {
            // we matched "e1.i(args)" -> method
            assert($args.tree != null);
            
            $tree = new MethodCall($e1.tree, $i.tree, $args.tree);
            setLocation($tree, $start);
        }
        | /* epsilon */ {
            // we matched "e.i" -> field

            $tree = new Selection($e1.tree, $i.tree);
            setLocation($tree, $start);
        }
        )
    ;

primary_expr returns[AbstractExpr tree]
    : ident {
            assert($ident.tree != null);

            $tree = $ident.tree;
        }
    | m=ident OPARENT args=list_expr CPARENT {
            assert($args.tree != null);
            assert($m.tree != null);
            //we watched ident(args) -> method
            
            This thisObj = new This();
            setLocation(thisObj, $start);
            $tree = new MethodCall(thisObj, $m.tree, $args.tree);
            setLocation($tree, $start);
        }
    | OPARENT expr CPARENT {
            assert($expr.tree != null);

            $tree = $expr.tree;
        }
    | READINT OPARENT CPARENT {
        $tree = new ReadInt();
        setLocation($tree, $start);
        }
    | READFLOAT OPARENT CPARENT {
        $tree = new ReadFloat();
        setLocation($tree, $start);
        }
    | NEW ident OPARENT CPARENT {
            assert($ident.tree != null);

            $tree = new New($ident.tree);
            setLocation($tree, $start);
        }
    | cast=OPARENT type CPARENT OPARENT expr CPARENT {
            assert($type.tree != null);
            assert($expr.tree != null);

            //TODO
        }
    | literal {
            assert($literal.tree != null);
            $tree= $literal.tree;
        }
    ;

type returns[AbstractIdentifier tree]
    : ident {
            assert($ident.tree != null);
            $tree = $ident.tree;
        }
    ;

literal returns[AbstractExpr tree]
    : INT {
        int i = Integer.parseInt($INT.getText());
        
        $tree = new IntLiteral(i);
        setLocation($tree, $start);
        }
    | fd=FLOAT {
        $tree = new FloatLiteral(Float.parseFloat($fd.getText()));
        setLocation($tree, $start);
        }
    | s=STRING {
        $tree = new StringLiteral($s.getText().substring($s.getText().indexOf('"') + 1, $s.getText().lastIndexOf('"')));
        setLocation($tree, $start);
        }
    | TRUE {
        $tree = new BooleanLiteral(true);
        setLocation($tree, $start);
        }
    | FALSE {
        $tree = new BooleanLiteral(false);
        setLocation($tree, $start);
        }
    | THIS {
        $tree = new This();
        setLocation($tree, $start);
        }
    | NULL {
        $tree = new NullExpr();
        setLocation($tree, $start);
        }
    ;

ident returns[AbstractIdentifier tree]
    : i=IDENT {
        $tree = new Identifier(getDecacCompiler().createSymbol($i.getText()));
        setLocation($tree, $start);
        }
    ;

/****     Class related rules     ****/

list_classes returns[ListDeclClass tree]
    @init{ $tree = new ListDeclClass();
    }
    : (c1=class_decl {
        assert($c1.tree != null);
        $tree.add($c1.tree);
        }
      )*
    ;

class_decl returns[DeclClass tree]
    : CLASS name=ident superclass=class_extension OBRACE class_body CBRACE {
        $tree = new DeclClass($name.tree, $superclass.tree, $class_body.methods, $class_body.fields);
        setLocation($tree, $start);
        }
    ;

class_extension returns[AbstractIdentifier tree]
    : EXTENDS ident {
        assert($ident.tree != null);
        $tree = $ident.tree;
        }
    | /* epsilon */ {
        //done return ObjectClass;
        $tree = getObjNameIdentifier();
        }
    ;

class_body returns[ListDeclMethod methods, ListDeclField fields]
@init{
    $methods = new ListDeclMethod();
    $fields = new ListDeclField();
}
    : (m=decl_method {
        assert($m.tree != null);
        $methods.add($m.tree);
        }
      | decl_field_set[$fields]
      )*
    ;

decl_field_set[ListDeclField l]
    : v=visibility t=type list_decl_field[$l, $v.tree, $t.tree]
      SEMI
    ;

visibility returns[Visibility tree]
    : /* epsilon */ {
        $tree = Visibility.PUBLIC;
        }
    | PROTECTED {
        $tree = Visibility.PROTECTED;
        }
    ;

list_decl_field[ListDeclField l, Visibility v, AbstractIdentifier t]
@init{ 
    assert($t != null);
    assert($v != null);
 }
    : dv1=decl_field[v, t]{
        assert($dv1.tree != null);
        $l.add($dv1.tree);
    }
        (COMMA dv2=decl_field[v, t]{
            assert($dv2.tree != null);
            $l.add($dv2.tree);
        }
      )*
    ;

decl_field[Visibility v, AbstractIdentifier t] returns[AbstractDeclField tree]
@init{ 
    AbstractInitialization init = null;
 }
 @after{
    if(init == null){
        init = new NoInitialization();
        setLocation(init, $start);
    }
    $tree = new DeclField($v, $t, $i.tree, init);
    setLocation($tree, $start);
 }
    : i=ident {
        assert($i.tree != null);
        }
      (EQUALS e=expr {
        assert($e.tree != null);
        init = new Initialization($e.tree);
        setLocation(init, $e.start);
        }
      )? 
    ;

decl_method returns[AbstractDeclMethod tree]
@init {
    AbstractMethodBody body;
}
@after{
    $tree = new DeclMethod($t.tree, $i.tree, $params.tree, body);
    setLocation($tree, $start);
}
    : t=type i=ident OPARENT params=list_params CPARENT (block {
        assert($i.tree != null);
        assert($t.tree != null);
        assert($params.tree != null);
        assert($block.decls != null);
        assert($block.insts != null);

        body = new MethodBody($block.decls, $block.insts);
        setLocation(body, $block.start);
        
        }
      | ASM OPARENT code=multi_line_string CPARENT SEMI {
        assert($i.tree != null);
        assert($t.tree != null);
        assert($params.tree != null);
        
        //convertir multi-line-string en literal
        String format = $code.text.substring($code.text.indexOf('"') + 1, $code.text.lastIndexOf('"'));
        StringLiteral asmInsts = new StringLiteral(format);
        setLocation(asmInsts, $code.start);

        body = new MethodAsmBody(asmInsts);
        }
      )
    ;

list_params returns[ListDeclParam tree]
@init{
    $tree = new ListDeclParam();
}
    : (p1=param {
        assert($p1.tree != null);
        $tree.add($p1.tree);
        } (COMMA p2=param {
            assert($p2.tree != null);
            $tree.add($p2.tree);
        }
      )*)?
    ;
    
multi_line_string returns[String text, Location location]
    : s=STRING {
            $text = $s.text;
            $location = tokenLocation($s);
        }
    | s=MULTI_LINE_STRING {
            $text = $s.text;
            $location = tokenLocation($s);
        }
    ;

param returns[DeclParam tree]
    : type ident {
        assert($type.tree != null);
        assert($ident.tree != null);
        $tree = new DeclParam($type.tree, $ident.tree);
        setLocation($tree, $start);
        }
    ;
