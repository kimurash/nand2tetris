import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private BufferedWriter buffWriter = null;
    
    public VMWriter(String filepath){
        try{
            this.buffWriter = new BufferedWriter(new FileWriter(filepath));
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void closeFile(){
        if(this.buffWriter != null){
            try{
                this.buffWriter.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void writePush(String segment, int idx){
        this.writeLine("push " + segment + " " + idx);
    }

    public void writePop(String segment, int idx){
        this.writeLine("pop " + segment + " " + idx);
    }

    public void writeArithmetic(String command){
        this.writeLine(command);
    }

    public void writeLabel(String label){
        this.writeLine("label " + label);
    }

    public void writeGoto(String label){
        this.writeLine("goto " + label);
    }

    public void writeIf(String label){
        this.writeLine("if-goto " + label);
    }

    public void writeCall(String name, int nArgs){
        this.writeLine("call " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nLocals){
        this.writeLine("function " + name + " " + nLocals);
    }

    public void writeReturn(){
        this.writeLine("return");
    }

    private void writeLine(String line){
        try{
            this.buffWriter.write(line);
            this.buffWriter.newLine();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
