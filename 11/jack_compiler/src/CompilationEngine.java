// import java.io.BufferedWriter;
// import java.io.FileWriter;
// import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;
    private VMWriter vmwriter;

    private String className;
    private int numLabel = 0;
    private boolean advanced = false;

    public CompilationEngine(File file){
        this.symbolTable = new SymbolTable();

        String path = file.getAbsolutePath();
        System.out.println(path);
        this.tokenizer = new JackTokenizer(path);

        String filename = path.substring(0, path.lastIndexOf("."));
        this.vmwriter = new VMWriter(filename + ".vm");
    }

    public void closeFile(){
        this.tokenizer.closeFile();
        this.vmwriter.closeFile();
    }

    /* program structure */
    public void compileClass(){
        this.compileKeyword(KeywordType.CLASS);
        this.className = this.compileIdentifier(); // className
        this.compileSymbol('{');

        while(this.isClassVarDec()){
            this.compilClassVarDec();
        }
        // System.out.println("---- class:" + this.className + " ----");
        // symbolTable.dumpClassTable();
        // System.out.println();

        while(this.isSubroutineDec()){
            this.compileSubroutineDec();
        }
        this.compileSymbol('}');
    }

    public void compilClassVarDec(){
        String kind = this.compileKeyword(KeywordType.STATIC, KeywordType.FIELD).toString().toLowerCase();
        String type = this.compileType();
        String name = this.compileIdentifier(); // varname
        symbolTable.define(name, type, kind);
        while(this.symbolIs(',')){
            this.compileSymbol(',');
            name = this.compileIdentifier();
            symbolTable.define(name, type, kind);
        }
        this.compileSymbol(';');
    }

    public void compileSubroutineDec(){
        symbolTable.startSubroutine();

        KeywordType subroutineType = this.compileKeyword(KeywordType.CONSTRUCTOR, KeywordType.FUNCTION, KeywordType.METHOD);
        if(this.keywordTypeIs(KeywordType.VOID)){
            this.compileKeyword(KeywordType.VOID);
        } else{
            this.compileType();
        }

        if(subroutineType == KeywordType.METHOD){
            symbolTable.define("this", this.className, "arg");
        }

        String subroutineName = this.compileIdentifier(); // subroutineName
        this.compileSymbol('(');
        this.compileParameterlist();
        this.compileSymbol(')');
        this.compileSubroutineBody(subroutineType, subroutineName);
    }

    public void compileParameterlist(){
        if(this.keywordTypeIs(KeywordType.INT, KeywordType.CHAR, KeywordType.BOOLEAN) ||
                this.tokenTypeIs(TokenType.IDENTIFIER)){
            String type = this.compileType();
            String name = this.compileIdentifier(); // varName
            symbolTable.define(name, type, "arg");

            while(this.symbolIs(',')){
                this.compileSymbol(',');
                type = this.compileType();
                name = this.compileIdentifier();
                symbolTable.define(name, type, "arg");
            }
        }
    }

    public void compileSubroutineBody(KeywordType subroutinType, String subroutineName){
        this.compileSymbol('{');
        int numLocal = 0;
        while(this.keywordTypeIs(KeywordType.VAR)){
            numLocal += this.compileVarDec();
        }

        this.vmwriter.writeFunction(this.className + "." + subroutineName, numLocal);

        if(subroutinType == KeywordType.METHOD){
            this.vmwriter.writePush("argument", 0);
            this.vmwriter.writePop("pointer", 0); // -> this
        } else if(subroutinType == KeywordType.CONSTRUCTOR){
            this.vmwriter.writePush("constant", symbolTable.varCount("field"));
            this.vmwriter.writeCall("Memory.alloc", 1);
            this.vmwriter.writePop("pointer", 0); // -> this
        } else if(subroutinType == KeywordType.FUNCTION){

        } else{
            System.out.println("compileSubroutineBody: Invalid subroutineType");
        }

        this.compileStatements();
        this.compileSymbol('}');

        // System.out.println("---- routine:" + subroutineName + " ----");
        // symbolTable.dumpSubroutineTable();
        // System.out.println();
    }

    public int compileVarDec(){
        this.compileKeyword(KeywordType.VAR);
        String type = this.compileType();
        String name = this.compileIdentifier(); // varName
        symbolTable.define(name, type, "var");
        int numVar = 1;

        while(this.symbolIs(',')){
            this.compileSymbol(',');
            name = this.compileIdentifier(); // varName
            symbolTable.define(name, type, "var");
            numVar++;
        }
        this.compileSymbol(';');
        return(numVar);
    }

    /* statement */
    public void compileStatements(){
        while(this.isStatement()){
            this.compileStatement();
        }
    }

    public void compileStatement(){
        if(this.keywordTypeIs(KeywordType.LET)){
            this.compileLet();
        } else if(this.keywordTypeIs(KeywordType.DO)){
            this.compileDo();
        } else if(this.keywordTypeIs(KeywordType.IF)){
            this.compileIf();
        } else if(this.keywordTypeIs(KeywordType.WHILE)){
            this.compileWhile();
        } else if(this.keywordTypeIs(KeywordType.RETURN)){
            this.compileReturn();
        }
    }

    public void compileLet(){
        this.compileKeyword(KeywordType.LET);
        String name = this.compileIdentifier(); // varName
        String kind = symbolTable.kindOf(name);
        if(this.symbolIs('[')){
            this.compileSymbol('[');
            this.compileExpression(); // offset
            this.compileSymbol(']');
            this.compileSymbol('=');

            // base address
            if(kind.equals("arg")){
                this.vmwriter.writePush("argument", symbolTable.indexOf(name));
            } else if(kind.equals("var")){
                this.vmwriter.writePush("local", symbolTable.indexOf(name));
            } else if(kind.equals("field")){
                this.vmwriter.writePush("this", symbolTable.indexOf(name));
            } else if(kind.equals("static")){
                this.vmwriter.writePush("static", symbolTable.indexOf(name));
            }

            // temp_2 <- base + offset
            this.vmwriter.writeArithmetic("add");
            this.vmwriter.writePop("temp", 2);

            this.compileExpression(); // value

            // that <- base + i
            this.vmwriter.writePush("temp", 2);
            this.vmwriter.writePop("pointer", 1);

            this.vmwriter.writePop("that", 0);
            this.compileSymbol(';');
        } else{
            this.compileSymbol('=');
            this.compileExpression();
            if(kind.equals("arg")){
                this.vmwriter.writePop("argument", symbolTable.indexOf(name));
            } else if(kind.equals("var")){
                this.vmwriter.writePop("local", symbolTable.indexOf(name));
            } else if(kind.equals("field")){
                this.vmwriter.writePop("this", symbolTable.indexOf(name));
            } else if(kind.equals("static")){
                this.vmwriter.writePop("static", symbolTable.indexOf(name));
            }
            this.compileSymbol(';');
        }
    }

    public void compileDo(){
        this.compileKeyword(KeywordType.DO);
        this.compileSubroutineCall(false, null);
        this.compileSymbol(';');
        this.vmwriter.writePop("temp", 0);
    }

    public void compileIf(){
        this.compileKeyword(KeywordType.IF);
        this.compileSymbol('(');
        this.compileExpression(); // cond
        this.compileSymbol(')');
        this.vmwriter.writeArithmetic("not"); // ~(cond)

        String label1 = this.issueNewLabel();
        String label2 = this.issueNewLabel();

        this.vmwriter.writeIf(label1);
        this.compileSymbol('{');
        this.compileStatements(); // s1
        this.compileSymbol('}');
        this.vmwriter.writeGoto(label2);
        this.vmwriter.writeLabel(label1);

        if(this.keywordTypeIs(KeywordType.ELSE)){
            this.compileKeyword(KeywordType.ELSE);
            this.compileSymbol(('{'));
            this.compileStatements(); // s2
            this.compileSymbol('}');
        }
        this.vmwriter.writeLabel(label2);
    }

    public void compileWhile(){
        String label1 = this.issueNewLabel();
        String label2 = this.issueNewLabel();

        this.compileKeyword(KeywordType.WHILE);
        this.vmwriter.writeLabel(label1);
        this.compileSymbol('(');
        this.compileExpression(); // cond
        this.compileSymbol(')');
        this.vmwriter.writeArithmetic("not"); // ~(cond)
        this.vmwriter.writeIf(label2);

        this.compileSymbol('{');
        this.compileStatements(); // s1
        this.compileSymbol('}');
        this.vmwriter.writeGoto(label1);

        this.vmwriter.writeLabel(label2);
    }

    public void compileReturn(){
        this.compileKeyword(KeywordType.RETURN);
        if(!(this.symbolIs(';'))){
            this.compileExpression();
        } else{
            this.vmwriter.writePush("constant", 0);
        }
        this.compileSymbol(';');
        this.vmwriter.writeReturn();
    }

    /* expression */
    public void compileExpression(){
        this.compileTerm();
        while(this.symbolIs('+', '-', '*', '/', '&', '|', '<', '>', '=')){
            char operator = this.compileSymbol('+', '-', '*', '/', '&', '|', '<', '>', '=');
            this.compileTerm();
            switch(operator){
                case '+' : this.vmwriter.writeArithmetic("add");  break;
                case '-' : this.vmwriter.writeArithmetic("sub");  break;
                case '*' : this.vmwriter.writeCall("Math.multiply", 2);  break;
                case '/' : this.vmwriter.writeCall("Math.divide", 2);  break;
                case '&' : this.vmwriter.writeArithmetic("and");  break;
                case '|' : this.vmwriter.writeArithmetic("or");  break;
                case '<' : this.vmwriter.writeArithmetic("lt");  break;
                case '>' : this.vmwriter.writeArithmetic("gt");  break;
                case '=' : this.vmwriter.writeArithmetic("eq");  break;
                default : break;
            }
        }
    }

    public void compileTerm(){
        if(this.tokenTypeIs(TokenType.INT_CONST)){
            int value = this.compileIntConst();
            this.vmwriter.writePush("constant", value);
        } else if(this.tokenTypeIs(TokenType.STRING_CONST)){
            this.compileStringConst();
        } else if(this.keywordTypeIs(KeywordType.NULL)){
            this.compileKeyword(KeywordType.NULL);
            this.vmwriter.writePush("constant", 0);
        } else if(this.keywordTypeIs(KeywordType.THIS)){
            this.compileKeyword(KeywordType.THIS);
            this.vmwriter.writePush("pointer", 0);
        } else if(this.keywordTypeIs(KeywordType.TRUE)){
            this.compileKeyword(KeywordType.TRUE);
            this.vmwriter.writePush("constant", 1);
            this.vmwriter.writeArithmetic("neg");
        } else if(this.keywordTypeIs(KeywordType.FALSE)){
            this.compileKeyword(KeywordType.FALSE);
            this.vmwriter.writePush("constant", 0);
        } else if(this.tokenTypeIs(TokenType.IDENTIFIER)){
            String identifier = this.compileIdentifier(); // (className | varName) | subroutineName
            if(this.symbolIs('[')){
                this.compileDefinedVarName(identifier);
                this.compileSymbol('[');
                this.compileExpression();
                this.vmwriter.writeArithmetic("add");
                this.vmwriter.writePop("pointer", 1); // that
                this.vmwriter.writePush("that", 0);
                this.compileSymbol(']');
            } else if(this.symbolIs('(', '.')){
                this.compileSubroutineCall(true, identifier); // className | varName
            } else{
                this.compileDefinedVarName(identifier);
            }
        } else if(this.symbolIs('(')){
            this.compileSymbol('(');
            this.compileExpression();
            this.compileSymbol(')');
        } else if(this.symbolIs('~')){
            this.compileSymbol('~');
            this.compileTerm();
            this.vmwriter.writeArithmetic("not");
        } else if(this.symbolIs('-')){
            this.compileSymbol('-');
            this.compileTerm();
            this.vmwriter.writeArithmetic("neg");   
        } else{
            System.out.println("compileTerm: syntax error");
        }
    }

    public void compileSubroutineCall(boolean isTerm, String identifier){
        if(!(isTerm)){
            identifier = this.compileIdentifier(); // (className | varName) | subroutineName
        }
        if(this.symbolIs('(')){
            // identifier = subroutineName
            this.compileSymbol('(');
            this.vmwriter.writePush("pointer", 0); // <- this
            int numArg = this.compileExpressionList();
            this.compileSymbol(')');
            this.vmwriter.writeCall(this.className + "." + identifier, numArg + 1);
        } else{
            if(symbolTable.kindOf(identifier) != null){
                // identifier = varName
                this.compileDefinedVarName(identifier);
                this.compileSymbol('.');
                String subroutineName = this.compileIdentifier();
                this.compileSymbol('(');
                this.compileDefinedVarName(identifier);
                int numArg = this.compileExpressionList();
                this.compileSymbol(')');
                this.vmwriter.writeCall(symbolTable.typeOf(identifier) + "." + subroutineName, numArg + 1);
            } else{
                // identifier = className
                this.compileSymbol('.');
                String subroutineName = this.compileIdentifier();
                this.compileSymbol('(');
                int numArg = this.compileExpressionList();
                this.compileSymbol(')');
                this.vmwriter.writeCall(identifier + "." + subroutineName, numArg);
            }
        }
    }

    public int compileExpressionList(){
        int numArg = 0;

        if(!(this.symbolIs(')'))){
            this.compileExpression();
            numArg++;
            while(this.symbolIs(',')){
                this.compileSymbol(',');
                this.compileExpression();
                numArg++;
            }
        }
        return(numArg);
    }

    public void compileDefinedVarName(String identifier){
        String kind = symbolTable.kindOf(identifier);
        if(kind.equals("arg")){
            this.vmwriter.writePush("argument", symbolTable.indexOf(identifier));
        } else if(kind.equals("var")){
            this.vmwriter.writePush("local", symbolTable.indexOf(identifier));
        } else if(kind.equals("field")){
            this.vmwriter.writePush("this", symbolTable.indexOf(identifier));
        } else if(kind.equals("static")){
            this.vmwriter.writePush("static", symbolTable.indexOf(identifier));
        }
    }

    /* private method */
    private boolean isAdvanced(){
        if(this.advanced){
            return(true);
        } else{
            if(tokenizer.advance()){
                this.advanced = true;
                return(true);
            } else{
                return(false);
            }
        }
    }

    private boolean tokenTypeIs(TokenType type){
        if(this.isAdvanced()){
            if(tokenizer.tokenType() == type){
                return(true);
            } else{
                return(false);
            }
        } else{
            return(false);
        }
    }

    private boolean keywordTypeIs(KeywordType... types){
        if(this.isAdvanced()){
            KeywordType crrntType = tokenizer.keywordType();
            for(KeywordType type : types){
                if(crrntType == type){
                    return(true);
                }
            }
            return(false);
        } else{
            return(false);
        }
    }

    private boolean symbolIs(char... symbols){
        if(this.isAdvanced()){
            char head = tokenizer.token().charAt(0);
            for(char c : symbols){
                if(head == c){
                    return(true);
                }
            }
            return(false);
        } else{
            return(false);
        }
    }

    private boolean isStatement(){
        if(this.isAdvanced()){
            ArrayList<KeywordType> typeList = new ArrayList<KeywordType>();
            typeList.add(KeywordType.LET);
            typeList.add(KeywordType.DO);
            typeList.add(KeywordType.IF);
            typeList.add(KeywordType.WHILE);
            typeList.add(KeywordType.RETURN);
            if(typeList.contains(tokenizer.keywordType())){
                return(true);
            } else{
                return(false);
            }
        } else{
            return(false);
        }
    }

    private boolean isClassVarDec(){
        if(this.isAdvanced()){
            ArrayList<KeywordType> typeList = new ArrayList<KeywordType>();
            typeList.add(KeywordType.STATIC);
            typeList.add(KeywordType.FIELD);
            if(typeList.contains(tokenizer.keywordType())){
                return(true);
            } else{
                return(false);
            }
        } else{
            return(false);
        }
        
    }

    private boolean isSubroutineDec(){
        if(this.isAdvanced()){
            ArrayList<KeywordType> typeList = new ArrayList<KeywordType>();
            typeList.add(KeywordType.CONSTRUCTOR);
            typeList.add(KeywordType.FUNCTION);
            typeList.add(KeywordType.METHOD);
            if(typeList.contains(tokenizer.keywordType())){
                return(true);
            } else{
                return(false);
            }
        } else{
            return(false);
        }
    }

    private String compileType(){
        if(this.keywordTypeIs(KeywordType.INT, KeywordType.CHAR, KeywordType.BOOLEAN)){
            return(this.compileKeyword(KeywordType.INT, KeywordType.CHAR, KeywordType.BOOLEAN)).toString().toLowerCase();
        } else{
            return(this.compileIdentifier());
        }
    }

    private KeywordType compileKeyword(KeywordType... types){
        if(this.isAdvanced()){
            for(KeywordType type : types){
                if(tokenizer.keywordType() == type){
                    this.advanced = false;
                    return(type);
                }
            }
            System.out.println("compileKeyword: syntax error");
            return(null);
        } else{
            return(null);
        }
    }

    private char compileSymbol(char... symbols){
        if(this.isAdvanced()){
            for(Character symbol : symbols){
                char head = tokenizer.token().charAt(0);
                if(head == symbol){
                    this.advanced = false;
                    return(symbol);
                }
            }
            System.out.println("compileSymbol: syntax error");
            return(0);
        } else{
            return(0);
        }
    }

    private String compileIdentifier(){
        if(this.isAdvanced()){
            if(tokenizer.tokenType() == TokenType.IDENTIFIER){
                this.advanced = false;
                return(tokenizer.token());
            } else{
                System.out.println("compileIdentifier: syntax error");
                return(null);
            }
        } else{
            return(null);
        }
    }

    private int compileIntConst(){
        if(this.isAdvanced()){
            if(tokenizer.tokenType() == TokenType.INT_CONST){
                this.advanced = false;
                return(Integer.parseInt(tokenizer.token()));                
            } else{
                System.out.println("compileIntConst: syntax error");
                return(0);
            }
        } else{
            return(0);
        }
    }

    private void compileStringConst(){
        if(isAdvanced()){
            if(tokenizer.tokenType() == TokenType.STRING_CONST){
                String string = tokenizer.token();
                this.vmwriter.writePush("constant", string.length());
                this.vmwriter.writeCall("String.new", 1);
                for(int i=0; i < string.length(); i++){
                    this.vmwriter.writePush("constant", string.codePointAt(i));
                    this.vmwriter.writeCall("String.appendChar", 2);
                }
            } else{
                System.out.println("compileStringConst: syntax error");
            }
            this.advanced = false;
        }
    }

    private String issueNewLabel(){
        return("LABEL_" + this.numLabel++);
    }
}
