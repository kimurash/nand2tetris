import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
// import java.util.List;
import java.util.ArrayList;
// import java.util.Arrays;
import java.io.File;

public class CompilationEngine {
    private JackTokenizer tokenizer = null;
    private BufferedWriter buffWriter = null;
    private boolean advanced = false;  

    public CompilationEngine(File file){
        String path = file.getAbsolutePath();
        this.tokenizer = new JackTokenizer(path);
        String filename = path.substring(0, path.lastIndexOf("."));
        try{
            this.buffWriter = new BufferedWriter(new FileWriter(filename + ".xml"));
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void closeFile(){
        try{
            if(this.buffWriter != null){
                this.buffWriter.close();
            }
            this.tokenizer.closeFile();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /* program structure */
    public void compileClass(){
        this.writeElementStart("class");

        this.compileKeyword(KeywordType.CLASS);
        this.compileIdentifier(); // className
        this.compileSymbol('{');

        while(this.isClassVarDec()){
            this.compilClassVarDec();
        }

        while(this.isSubroutineDec()){
            this.compileSubroutineDec();
        }
        this.compileSymbol('}');

        this.writeElementEnd("class");
    }

    public void compilClassVarDec(){
        this.writeElementStart("classVarDec");

        this.compileKeyword(KeywordType.STATIC, KeywordType.FIELD);
        this.compileType();
        this.compileIdentifier(); // varname
        while(this.symbolIs(',')){
            this.compileSymbol(',');
            this.compileIdentifier();
        }
        this.compileSymbol(';');

        this.writeElementEnd("classVarDec");
    }

    public void compileSubroutineDec(){
        this.writeElementStart("subroutineDec");

        this.compileKeyword(KeywordType.CONSTRUCTOR, KeywordType.FUNCTION, KeywordType.METHOD);
        if(this.keywordTypeIs(KeywordType.VOID)){
            this.compileKeyword(KeywordType.VOID);
        } else{
            this.compileType();
        }
        this.compileIdentifier(); // subroutineName
        this.compileSymbol('(');
        this.compileParameterlist();
        this.compileSymbol(')');
        this.compileSubroutineBody();

        this.writeElementEnd("subroutineDec");
    }

    public void compileParameterlist(){
        this.writeElementStart("parameterList");

        if(this.keywordTypeIs(KeywordType.INT, KeywordType.CHAR, KeywordType.BOOLEAN) ||
                this.tokenTypeIs(TokenType.IDENTIFIER)){
            this.compileType();
            this.compileIdentifier(); // varName

            while(this.symbolIs(',')){
                this.compileSymbol(',');
                this.compileType();
                this.compileIdentifier();
            }
        }

        this.writeElementEnd("parameterList");
    }

    public void compileSubroutineBody(){
        this.writeElementStart("subroutineBody");

        this.compileSymbol('{');
        while(this.keywordTypeIs(KeywordType.VAR)){
            this.compileVarDec();
        }
        this.compileStatements();
        this.compileSymbol('}');

        this.writeElementEnd("subroutineBody");
    }

    public void compileVarDec(){
        this.writeElementStart("varDec");

        this.compileKeyword(KeywordType.VAR);
        this.compileType();
        this.compileIdentifier(); // varName
        while(this.symbolIs(',')){
            this.compileSymbol(',');
            this.compileIdentifier(); // varName
        }
        this.compileSymbol(';');

        this.writeElementEnd("varDec");
    }

    /* statement */
    public void compileStatements(){
        this.writeElementStart("statements");

        while(this.isStatement()){
            this.compileStatement();
        }

        this.writeElementEnd("statements");
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
        this.writeElementStart("letStatement");

        this.compileKeyword(KeywordType.LET);
        this.compileIdentifier(); // varName
        if(this.symbolIs('[')){
            this.compileSymbol('[');
            this.compileExpression();
            this.compileSymbol(']');
        }
        this.compileSymbol('=');
        this.compileExpression();
        this.compileSymbol(';');

        this.writeElementEnd("letStatement");
    }

    public void compileDo(){
        this.writeElementStart("doStatement");

        this.compileKeyword(KeywordType.DO);
        this.compileSubroutineCall(false);
        this.compileSymbol(';');

        this.writeElementEnd("doStatement");
    }

    public void compileIf(){
        this.writeElementStart("ifStatement");

        this.compileKeyword(KeywordType.IF);
        this.compileSymbol('(');
        this.compileExpression();
        this.compileSymbol(')');
        this.compileSymbol('{');
        this.compileStatements();
        this.compileSymbol('}');
        if(this.keywordTypeIs(KeywordType.ELSE)){
            this.compileKeyword(KeywordType.ELSE);
            this.compileSymbol(('{'));
            this.compileStatements();
            this.compileSymbol('}');
        }

        this.writeElementEnd("ifStatement");
    }

    public void compileWhile(){
        this.writeElementStart("whileStatement");

        this.compileKeyword(KeywordType.WHILE);
        this.compileSymbol('(');
        this.compileExpression();
        this.compileSymbol(')');
        this.compileSymbol('{');
        this.compileStatements();
        this.compileSymbol('}');

        this.writeElementEnd("whileStatement");
    }

    public void compileReturn(){
        this.writeElementStart("returnStatement");

        this.compileKeyword(KeywordType.RETURN);
        if(!(this.symbolIs(';'))){
            this.compileExpression();
        }
        this.compileSymbol(';');

        this.writeElementEnd("returnStatement");
    }

    /* expression */
    public void compileExpression(){
        this.writeElementStart("expression");

        this.compileTerm();
        while(this.symbolIs('+', '-', '*', '/', '&', '|', '<', '>', '=')){
            this.compileSymbol('+', '-', '*', '/', '&', '|', '<', '>', '=');
            this.compileTerm();
        }

        this.writeElementEnd("expression");
    }

    public void compileTerm(){
        this.writeElementStart("term");

        if(this.tokenTypeIs(TokenType.INT_CONST)){
            this.compileIntConst();
        } else if(this.tokenTypeIs(TokenType.STRING_CONST)){
            this.compileStringConst();
        } else if(this.keywordTypeIs(KeywordType.NULL, KeywordType.THIS, KeywordType.TRUE, KeywordType.FALSE)){
            this.compileKeyword(KeywordType.NULL, KeywordType.THIS, KeywordType.TRUE, KeywordType.FALSE);
        } else if(this.tokenTypeIs(TokenType.IDENTIFIER)){
            this.compileIdentifier();
            if(this.symbolIs('[')){
                this.compileSymbol('[');
                this.compileExpression();
                this.compileSymbol(']');
            } else if(this.symbolIs('(', '.')){
                this.compileSubroutineCall(true);
            }
        } else if(this.symbolIs('(')){
            this.compileSymbol('(');
            this.compileExpression();
            this.compileSymbol(')');
        } else if(this.symbolIs('~', '-')){
            this.compileSymbol('~', '-');
            this.compileTerm();
        } else{
            System.out.println("compileTerm: syntax error");
        }

        this.writeElementEnd("term");
    }

    public void compileSubroutineCall(boolean isTerm){
        if(!(isTerm)){
            this.compileIdentifier(); // subroutineName | (className | varName)
        }
        if(this.symbolIs('.')){
            this.compileSymbol('.');
            this.compileIdentifier(); // subroutineName   
        }
        this.compileSymbol('(');
        this.compileExpressionList();
        this.compileSymbol(')');
    }

    public void compileExpressionList(){
        this.writeElementStart("expressionList");

        if(!(this.symbolIs(')'))){
            this.compileExpression();
            while(this.symbolIs(',')){
                this.compileSymbol(',');
                this.compileExpression();
            }
        }

        this.writeElementEnd("expressionList");
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

    private void compileType(){
        if(this.keywordTypeIs(KeywordType.INT, KeywordType.CHAR, KeywordType.BOOLEAN)){
            this.compileKeyword(KeywordType.INT, KeywordType.CHAR, KeywordType.BOOLEAN);
        } else{
            this.compileIdentifier();
        }
    }

    private void compileKeyword(KeywordType... types){
        if(this.isAdvanced()){
            for(KeywordType type : types){
                if(tokenizer.keywordType() == type){
                    this.writeElement("keyword", tokenizer.convertToken());
                    this.advanced = false;
                    return;
                }
            }
            System.out.println("compileKeyword: syntax error");
        }
    }

    private void compileSymbol(char... symbols){
        if(this.isAdvanced()){
            for(Character symbol : symbols){
                char head = tokenizer.token().charAt(0);
                if(head == symbol){
                    this.writeElement("symbol", tokenizer.convertToken());
                    this.advanced = false;
                    return;
                }
            }
            System.out.println("compileSymbol: syntax error");
        }
    }

    private void compileIdentifier(){
        if(this.isAdvanced()){
            if(tokenizer.tokenType() == TokenType.IDENTIFIER){
                this.writeElement("identifier", tokenizer.convertToken());
            } else{
                System.out.println("compileIdentifier: syntax error");
            }
            this.advanced = false;
        }
    }

    private void compileIntConst(){
        if(this.isAdvanced()){
            if(tokenizer.tokenType() == TokenType.INT_CONST){
                this.writeElement("integerConstant", tokenizer.convertToken());
            } else{
                System.out.println("compileIntConst: syntax error");
            }
            this.advanced = false;
        }
    }

    private void compileStringConst(){
        if(isAdvanced()){
            if(tokenizer.tokenType() == TokenType.STRING_CONST){
                this.writeElement("stringConstant", tokenizer.convertToken());
            } else{
                System.out.println("compileStringConst: syntax error");
            }
            this.advanced = false;
        }
    }

    private void writeElement(String elem, String token){
        try{
            this.buffWriter.write("<" + elem + "> " + token + " </" + elem + ">");
            this.buffWriter.newLine();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void writeElementStart(String elem){
        try{
            this.buffWriter.write("<" + elem + ">");
            this.buffWriter.newLine();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void writeElementEnd(String elem){
        try{
            this.buffWriter.write("</" + elem + ">");
            this.buffWriter.newLine();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
