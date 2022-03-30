import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class JackTokenizer{
    private static final HashMap<String, String> convertMap  = new HashMap<>();
    private static final HashMap<String, KeywordType> keywords = new HashMap<>();
    private static final char[] symbols = {
        '{', '}', '(', ')', '[', ']', '.', ',',
        ';', '+', '-', '*', '/', '&', '|',
        '<', '>', '=', '~'};

    private BufferedReader buffReader = null;
    private boolean within_comment = false;
    private String remain;
    private String crrntToken;
    private TokenType crrntType;

    public JackTokenizer(String filepath){
        keywords.put("class", KeywordType.CLASS);
        keywords.put("constructor", KeywordType.CONSTRUCTOR);
        keywords.put("function", KeywordType.FUNCTION);
        keywords.put("method", KeywordType.METHOD);
        keywords.put("field", KeywordType.FIELD);
        keywords.put("static", KeywordType.STATIC);
        keywords.put("var", KeywordType.VAR);
        keywords.put("int", KeywordType.INT);
        keywords.put("char", KeywordType.CHAR);
        keywords.put("boolean", KeywordType.BOOLEAN);
        keywords.put("void", KeywordType.VOID);
        keywords.put("true", KeywordType.TRUE);
        keywords.put("false", KeywordType.FALSE);
        keywords.put("null", KeywordType.NULL);
        keywords.put("this", KeywordType.THIS);
        keywords.put("let", KeywordType.LET);
        keywords.put("do", KeywordType.DO);
        keywords.put("if", KeywordType.IF);
        keywords.put("else", KeywordType.ELSE);
        keywords.put("while", KeywordType.WHILE);
        keywords.put("return", KeywordType.RETURN);

        convertMap.put("&", "&amp;");
        convertMap.put("<", "&lt;");
        convertMap.put(">", "&gt;");
        try{
            this.buffReader = new BufferedReader(new FileReader(filepath));
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void closeFile(){
        try{
            if(this.buffReader != null){
                this.buffReader.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public boolean hasMoreTokens(){
        try{
            while(true){
                remain = buffReader.readLine();
                if(remain == null){
                    return(false);
                }
                this.removeComment();
                remain = remain.trim();
                if(!(remain.equals(""))){ break; }
            }
            System.out.println();
            System.out.println("remain:" + remain);
            return(true);
        } catch(IOException e){
            e.printStackTrace();
            return(false);
        }
    }

    public boolean advance(){
        if((remain != null) && !(remain.equals(""))){
            remain = remain.trim();
            char head = remain.charAt(0);
            if(this.cutOutSymbol(head)){
                crrntType = TokenType.SYMBOL;
            } else if(this.cutOutInteger(head)){
                crrntType = TokenType.INT_CONST;
            } else if(this.cutOutString(head)){
                crrntType = TokenType.STRING_CONST;
            } else if(this.cutOutKeyword()){
                crrntType = TokenType.KEYWORD;
            } else if(this.cutOutIdentifier(head)){
                crrntType = TokenType.IDENTIFIER;
            } else{
                System.err.println("Unknown token exists.");
                System.exit(-1);
            }
            return(true);
        } else{
            if(this.hasMoreTokens()){
                return(this.advance());
            } else{
                return(false);
            }
        }
    }

    private void removeComment(){
        int idx = remain.indexOf("//");
        if(idx != -1){ remain = remain.substring(0, idx); }

        if(this.within_comment){
            if(remain.contains("*/")){
                int endidx = remain.indexOf("*/");
                remain = remain.substring(endidx + 2);
                this.within_comment = false;
            } else{
                remain = "";
            }
        } else{
            if(remain.contains("/*") || remain.contains("/**")){
                int beginidx = remain.indexOf("/*");
                if(remain.contains("*/")){
                    int endidx = remain.indexOf("*/");
                    remain = remain.substring(0, beginidx)
                                + remain.substring(endidx + 2);
                } else{
                    this.within_comment = true;
                    remain = remain.substring(0, beginidx);
                }
            }
        }
    }

    private boolean cutOutSymbol(char head){
        for(char c : symbols){
            if(head == c){
                crrntToken = String.valueOf(c);
                remain = remain.substring(1);
                System.out.println("symbol:" + crrntToken);
                System.out.println("remain:" + remain);
                return(true);
            }
        }
        return(false);
    }

    private boolean cutOutInteger(char head){
        if(Character.isDigit(head)){
            int i = 1;
            char temp = remain.charAt(i);
            StringBuilder builder = new StringBuilder(String.valueOf(head));
            while(Character.isDigit(temp) && i < remain.length()){
                builder.append(temp);
                temp = remain.charAt(++i);
            }
            crrntToken = builder.toString();
            remain = remain.substring(crrntToken.length());
            System.out.println("integer:" + crrntToken);
            System.out.println("remain:" + remain);
            return(true);
        } else{
            return(false);
        }
    }

    private boolean cutOutString(char head){
        if(head == '"'){
            int lastidx = remain.indexOf('"', 1);
            // System.out.println("lastidx:" + lastidx);
            crrntToken = remain.substring(1, lastidx);
            remain = remain.substring(lastidx + 1);
            System.out.println("string:" + crrntToken);
            System.out.println("remain:" + remain);
            return(true);
        } else{
            return(false);
        }
    }

    private boolean cutOutKeyword(){
        for(String keyword : keywords.keySet()){
            int i;
            for(i=0; i < keyword.length(); i++){
                if(!(remain.charAt(i) == keyword.charAt(i))){
                    break;
                }
            }
            if(i >= keyword.length()){
                crrntToken = keyword;
                remain = remain.substring(crrntToken.length());
                System.out.println("keyword:" + crrntToken);
                System.out.println("remain:" + remain);
                return(true);
            }
        }
        return(false);
    }

    private boolean cutOutIdentifier(char head){
        if(Character.isLetter(head) || head == '_'){
            int i = 1;
            char temp = remain.charAt(i);
            StringBuilder builder = new StringBuilder(String.valueOf(head));
            while((Character.isLetterOrDigit(temp) || temp == '_') &&  i < remain.length()){
                builder.append(temp);
                temp = remain.charAt(++i);
            }
            crrntToken = builder.toString();
            remain = remain.substring(crrntToken.length());
            System.out.println("identifier:" + crrntToken);
            System.out.println("remain:" + remain);
            return(true);
        } else{
            return(false);
        }
    }

    public String convertToken(){
        if(convertMap.containsKey(this.crrntToken)){
            return(convertMap.get(this.crrntToken));
        } else{
            return(this.crrntToken);
        }
    }

    public String token(){
        return(this.crrntToken);
    }

    public TokenType tokenType(){
        return(this.crrntType);
    }

    public KeywordType keywordType(){
        if(keywords.containsKey(crrntToken)){
            return(keywords.get(crrntToken));
        } else{
            return(null);
        }
        
    }

    // public char symbol(){ }
    // public String identifier(){ }
    // public int intVal(){ }
    // public String stringVal(){ }

    public static void main(String[] args) {
        File file = new File(args[0]);
        if(file.isFile()){
            JackTokenizer tokenizer = new JackTokenizer(args[0]);
            String filename = args[0].substring(0, args[0].lastIndexOf("."));
            try{
                BufferedWriter buffWriter = new BufferedWriter(new FileWriter(filename + "T.xml"));
                buffWriter.write("<tokens>");
                buffWriter.newLine();
                String element = null;
                while(tokenizer.advance()){
                    int id = tokenizer.tokenType().getId();
                    switch(id){
                        case 0 : // KEYWORD
                            element = "keyword";
                            break;
                        case 1 : // SYMBOL
                            element = "symbol";
                            break;
                        case 2 : // INT_CONST
                            element = "integerConstant";
                            break;
                        case 3 : // STRING_CONST
                            element = "stringConstant";
                            break;
                        case 4 : // IDENTIFIER
                            element = "identifier";
                            break;
                        default : break;
                    }
                    buffWriter.write("<" + element + "> " + tokenizer.convertToken() + " </" + element + ">");
                    buffWriter.newLine();
                    System.out.println();
                }
                buffWriter.write("</tokens>");
                buffWriter.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        } else{
            System.out.println("Usage: JackTokenizer filepath");
        }
    }
}