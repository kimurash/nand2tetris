import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class CodeWriter {
    private static final int POINTER_BASE_ADDRESS = 3;
    private static final int TEMP_BASE_ADDRESS = 5;

    private BufferedWriter  buffWriter = null;
    private String fname;
    private int nlabel = 0;

    // public CodeWriter(String filename){
    //     this.fname = filename;
    //     try{
    //         this.buffWriter = new BufferedWriter(new FileWriter(this.fname + ".asm"));
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
            // System.out.println(path);
            this.buffWriter = new BufferedWriter(new FileWriter(path + this.fname + ".asm"));
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
        this.fname = filename;
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
                String[] codes = {"@"+index, "D=A"};
                this.writeCodes(codes);
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
                        this.writeCode("@" + this.fname + "." + index);
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
                    String[] codes = {"D=M", "@" + this.fname + "." + index, "M=D"};
                    this.writeCodes(codes);
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
        String label1 = this.issueNewLabel();
        String label2 = this.issueNewLabel();
        String type = null;
        switch(cmd){
            case "eq" : type = "JEQ";  break;
            case "gt" : type = "JGT";  break;
            case "lt" : type = "JLT";  break;
            default : break;
        }
        String[] codes = {
            "D=M-D", "@"+label1, "D;"+type, "D=0", "@"+label2,
            "0;JMP", "("+label1+")", "D=-1", "("+label2+")"};
        this.writeCodes(codes);
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
        String[] codes = {"@"+symbol, "A=M"};
        this.writeCodes(codes);
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
        String[] codes = {"D=M", "@"+symbol, "A=M"};
        this.writeCodes(codes);
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
        String[] codes = {"@SP", "A=M", "M=D", "@SP", "M=M+1"};
        this.writeCodes(codes);
    }

    private void writePopToData(){
        String[] codes = {"@SP", "M=M-1", "A=M"};
        this.writeCodes(codes);
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

    private void writeCodes(String[] codes){
        for(int i=0; i < codes.length; i++){
            this.writeCode(codes[i]);
        }
    }

    private String issueNewLabel(){
        return("LABEL" + (this.nlabel++));
    }
}
