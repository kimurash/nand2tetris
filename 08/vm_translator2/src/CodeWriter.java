import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class CodeWriter {
    private static final int POINTER_BASE_ADDRESS = 3;
    private static final int TEMP_BASE_ADDRESS = 5;

    private BufferedWriter  buffWriter = null;
    private String filename;
    private String funcname;
    private int numIfLabel = 0;
    private int numReturnLabel = 0;

    // public CodeWriter(String filename){
    //     this.filename = filename;
    //     try{
    //         this.buffWriter = new BufferedWriter(new FileWriter(this.filename + ".asm"));
    //     } catch(IOException e){
    //         e.printStackTrace();
    //     }
    // }

    public CodeWriter(File file){
        String filename = file.getName();
        String path = file.getAbsolutePath();
        if(file.isDirectory()){
            this.setFileName(filename);
            path = path.concat("\\");
        } else{
            this.setFileName(filename.substring(0, filename.lastIndexOf(".")));
            path = path.substring(0, path.lastIndexOf("\\") + 1);
        }
        try{
            System.out.println(path);
            this.buffWriter = new BufferedWriter(new FileWriter(path + this.filename + ".asm"));
            this.writeInit();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void closeFile(){
        try{
            if(this.buffWriter != null){
                this.buffWriter.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void setFileName(String filename){
        this.filename = filename;
        // this.closeFile();
    }

    public void writeArithmetic(String command){
        switch(command){
            case "add" :
            case "sub" :
            case "and" :
            case "or" : this.writeBinaryOperation(command);  break;
            case "neg" :
            case "not" : this.writeUnaryOperation(command);  break;
            case "eq" :
            case "gt" :
            case "lt" : this.writeCompOperation(command);  break;
            default : break;
        }
    }

    public void writePushPop(CommandType type, String segment, int index){
        int id = type.getId();
        if(id == 1){ // C_PUSH
            if(segment.equals("constant")){
                this.writeCodes("@"+index, "D=A");
                this.writePushFromData();
            } else{
                switch(segment){
                    case "local" :
                    case "argument" :
                    case "this" :
                    case "that" :
                        this.writePushFromDynamic(segment, index);
                        break;
                    case "pointer" :
                    case "temp" :
                        this.writePushFromStatic(segment, index);
                        break;
                    case "static" :
                        this.writeCode("@" + this.filename + "." + index);
                        this.writeCode("D=M");
                        this.writePushFromData();
                        break;
                    default : break;
                }
            }
        } else if(id == 2){ // C_POP
            switch(segment){
                case "local" :
                case "argument" :
                case "this" :
                case "that" :
                    this.writePopToDynamic(segment, index);
                    break;
                case "pointer" :
                case "temp" :
                    this.writePopToStatic(segment, index);
                    break;
                case "static" :
                    this.writePopToData();
                    this.writeCodes("D=M", "@" + this.filename + "." + index, "M=D");
                    break;
                default : break;
            }
        }
    }

    private void writeBinaryOperation(String cmd){
        this.writePopToData();
        this.writeCode("D=M");
        this.writePopToData();
        switch(cmd){
            case "add" : this.writeCode("D=M+D");  break;
            case "sub" : this.writeCode("D=M-D");  break;
            case "and" : this.writeCode("D=M&D");  break;
            case "or" : this.writeCode("D=M|D");  break;
            default : break;
        }
        this.writePushFromData();
    }

    private void writeUnaryOperation(String cmd){
        this.writePopToData();
        this.writeCode("D=M");
        switch(cmd){
            case "neg" : this.writeCode("D=-D");  break;
            case "not" : this.writeCode("D=!D");  break;
            default : break;
        }
        this.writePushFromData();
    }

    private void writeCompOperation(String cmd){
        this.writePopToData();
        this.writeCode("D=M");
        this.writePopToData();
        String label1 = this.issueNewIfLabel();
        String label2 = this.issueNewIfLabel();
        String type = null;
        switch(cmd){
            case "eq" : type = "JEQ";  break;
            case "gt" : type = "JGT";  break;
            case "lt" : type = "JLT";  break;
            default : break;
        }
        this.writeCodes( //
            "D=M-D", "@"+label1, "D;"+type, "D=0", "@"+label2,
            "0;JMP", "("+label1+")", "D=-1", "("+label2+")");
        this.writePushFromData();
    }

    private void writePushFromDynamic(String segment, int index){
        String symbol = null;
        switch(segment){
            case "local" : symbol = "LCL";  break;
            case "argument" : symbol = "ARG";  break;
            case "this" : symbol = "THIS";  break;
            case "that" : symbol = "THAT";  break;
            default : break;
        }
        this.writeCodes("@"+symbol, "A=M");
        for(int i=0; i < index;  i++){
            this.writeCode("A=A+1");
        }
        this.writeCode("D=M");
        this.writePushFromData();
    }

    private void writePopToDynamic(String segment, int index){
        String symbol = null;
        switch(segment){
            case "local" : symbol = "LCL";  break;
            case "argument" : symbol = "ARG";  break;
            case "this" : symbol = "THIS";  break;
            case "that" : symbol = "THAT";  break;
            default : break;
        }
        this.writePopToData();
        this.writeCodes("D=M", "@"+symbol, "A=M");
        for(int i=0; i < index; i++){
            writeCode("A=A+1");
        }
        writeCode("M=D");
    }

    private void writePushFromStatic(String segment, int index){
        int base = 0;
        switch(segment){
            case "pointer" : base = CodeWriter.POINTER_BASE_ADDRESS;  break;
            case "temp" : base = CodeWriter.TEMP_BASE_ADDRESS;  break;
            default : break;
        }
        this.writeCode("@" + base);
        for(int i=0; i < index; i++){
            this.writeCode("A=A+1");
        }
        this.writeCode("D=M");
        this.writePushFromData();
    }

    private void writePopToStatic(String segment, int index){
        this.writePopToData();
        this.writeCode("D=M");
        int base = 0;
        switch(segment){
            case "pointer" : base = CodeWriter.POINTER_BASE_ADDRESS;  break;
            case "temp" : base = CodeWriter.TEMP_BASE_ADDRESS;  break;
            default : break;
        }
        this.writeCode("@" + base);
        for(int i=0; i < index; i++){
            this.writeCode("A=A+1");
        }
        this.writeCode("M=D");
    }

    private void writePushFromData(){
        this.writeCodes("@SP", "A=M", "M=D", "@SP", "M=M+1");
    }

    private void writePopToData(){
        this.writeCodes("@SP", "M=M-1", "A=M");
    }

    /* chap08 */
    public void writeInit(){
        this.writeCodes("@256", "D=A", "@SP", "M=D");
        this.writeCall("Sys.init", 0);
    }

    public void writeLabel(String label){
        this.writeCode("(" + this.getLabelName(label) + ")");
    }

    public void writeGoto(String label){
        this.writeCodes("@" + this.getLabelName(label), "0;JMP");
    }

    public void writeIf(String label){
        this.writePopToData();
        this.writeCodes("D=M", "@" + this.getLabelName(label), "D;JNE");
    }

    public void writeCall(String functionName, int numArgs){
        String returnLabel = this.issueNewReturnLabel();
        this.writeCodes("@"+returnLabel, "D=A");
        this.writePushFromData(); // push return-address
        
        String[] symbol = {"LCL", "ARG", "THIS", "THAT"};
        for(int i=0; i < symbol.length; i++){
            this.writeCodes("@"+symbol[i], "D=M");
            this.writePushFromData();
        }

        this.writeCodes( //
            "@SP", "D=M", "@" + numArgs, "D=D-A",
            "@5", "D=D-A", "@ARG", "M=D", // ARG = SP - n - 5
            "@SP", "D=M", "@LCL", "M=D", // LCL = SP
            "@" + functionName, "0;JMP", // goto f
            "(" + returnLabel + ")"); // (return-address)
    }

    public void writeReturn(){
        this.writeCodes( //
            "@LCL", "D=M", "@R13", "M=D", // R13 = LCL
            "@5", "D=D-A", "A=D", "D=M", "@R14", "M=D" // R14 = RET
        );
        this.writePopToData();
        this.writeCodes("D=M", "@ARG", "A=M", "M=D"); // *ARG = pop()
        this.writeCodes("D=A+1", "@SP", "M=D"); // SP = ARG + 1
        this.writeCodes("@R13", "AM=M-1", "D=M", "@THAT", "M=D"); // THAT = *(LCL - 1)
        this.writeCodes("@R13", "AM=M-1", "D=M", "@THIS", "M=D"); // THIS = *(LCL - 2)
        this.writeCodes("@R13", "AM=M-1", "D=M", "@ARG", "M=D"); //  ARG = *(LCL - 3)
        this.writeCodes("@R13", "AM=M-1", "D=M", "@LCL", "M=D"); //  LCL = *(LCL - 4)
        this.writeCodes("@R14", "A=M", "0;JMP"); // goto RET
    }

    public void writeFunction(String functionName, int numLocals){
        this.funcname = functionName;
        this.writeCodes("(" + functionName + ")", "D=0");
        for(int i=0; i < numLocals; i++){
            this.writePushFromData();
        }
    }

    private void writeCode(String code){
        try{
            System.out.println(code);
            buffWriter.write(code);
            buffWriter.newLine();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void writeCodes(String... codes){
        for(int i=0; i < codes.length; i++){
            this.writeCode(codes[i]);
        }
    }

    private String issueNewIfLabel(){
        return("IF_LABEL_" + (this.numIfLabel++));
    }

    private String issueNewReturnLabel(){
        return("RET_LABEL_" + (this.numReturnLabel++));
    }

    private String getLabelName(String label){
        if(this.funcname != null){
            return(this.funcname + "$" + label);
        } else{
            return(label);
        }
    }
}
