import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private final String[] arithmetic = {
        "add", "sub",
        "neg", "eq", "gt", "lt",
        "and", "or", "not"
    };

    private BufferedReader buffReader = null;
    private String crrnt_cmd;
    private String[] splitted;

    public Parser(String filepath){
        try{
            this.buffReader = new BufferedReader(new FileReader(filepath));
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void closeFile(){
        try{
            this.buffReader.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public boolean hasMoreCommands(){
        try{
            while(true){
                crrnt_cmd = buffReader.readLine();
                if(crrnt_cmd == null){
                    return(false);
                }

                // crrnt_cmd = crrnt_cmd.replace(" ", "");
                int idx = crrnt_cmd.indexOf("//");
                if(idx != -1){
                    crrnt_cmd = crrnt_cmd.substring(0, idx);
                }
                if(!(crrnt_cmd.equals(""))){ break; }
            }
            System.out.println(crrnt_cmd);
            return(true);
        } catch(IOException e){
            e.printStackTrace();
            return(false);
        }
    }

    // public void advance(){ }

    public CommandType commandType(){
        if(crrnt_cmd != null){
            this.splitted = crrnt_cmd.split(" ");
            // for(String cmd : splitted){
            //     System.out.println(cmd);
            // }

            for(int i=0; i < this.arithmetic.length; i++){
                if(splitted[0].equals(this.arithmetic[i])){
                    // System.out.println("C_ARITHMETIC");
                    return(CommandType.C_ARITHMETIC);
                }
            }

            if(splitted[0].equals("push")){
                // System.out.println("C_PUSH");
                return(CommandType.C_PUSH);
            } else if(splitted[0].equals("pop")){
                // System.out.println("C_POP");
                return(CommandType.C_POP);
            } else if(splitted[0].equals("label")){
                return(CommandType.C_LABEL);
            } else if(splitted[0].equals("goto")){
                return(CommandType.C_GOTO);
            } else if(splitted[0].equals("if-goto")){
                return(CommandType.C_IF);
            } else if(splitted[0].equals("function")){
                return(CommandType.C_FUNCTION);
            } else if(splitted[0].equals("call")){
                return(CommandType.C_CALL);
            } else if(splitted[0].equals("return")){
                return(CommandType.C_RETURN);
            } else{
                return(CommandType.UNMATCH);
            }
        } else{
            return(null);
        }
    }

    public String arg1(){
        int id = this.commandType().getId();
        System.out.print("arg1:");
        if(id != 7){
            if(id == 0){ // C_ARITHMETIC
                System.out.println(splitted[0]);
                return(splitted[0]);
            } else{ // others
                System.out.println(splitted[1]);
                return(splitted[1]);
            }
        } else{ // C_RETURN
            System.out.println("Command doesn't have an arg1.");
            return(null);
        }
    }

    public int arg2(){
        int id = this.commandType().getId();
        System.out.print("arg2:");
        //  C_PUSH      C_POP      C_FUNCTION   C_CALL
        if((id == 1) | (id == 2) | (id == 6) | (id == 8)){
            System.out.println(splitted[2]);
            return(Integer.parseInt(splitted[2].trim()));
        } else{
            System.out.println("Command doesn't have an arg2.");
            // System.exit(-1);
            return(-1);
        }
    }
}
