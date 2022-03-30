import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private static final String PATTERN_A = "@([0-9a-zA-Z_\\.\\$:]+)";
    private static final String PATTERN_L = "\\(([0-9a-zA-Z_\\.\\$:]*)\\)";
    private static final String PATTERN_C = "((A?M?D?)=)?([^;]+)(;(.+))?";

    private BufferedReader buffReader = null;
    private String crrnt_cmd;
    // private int nline = 1;

    private String dest, comp, jump;

    public Parser(String filepath){
        try{
            this.buffReader = new BufferedReader(new FileReader(filepath));
            buffReader.mark(1024); // < 8192
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public boolean hasMoreCommands(){
        try{
            while(true){
                crrnt_cmd = buffReader.readLine();
                // System.out.println("before:" + crrnt_cmd);
                if(crrnt_cmd == null){
                    return(false);
                }

                crrnt_cmd = crrnt_cmd.replace(" ", "");
                int idx = crrnt_cmd.indexOf("//");
                if(idx != -1){
                    crrnt_cmd =  crrnt_cmd.substring(0, idx);
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
            if(crrnt_cmd.matches(PATTERN_A)){
                System.out.println("A_COMMAND");
                return(CommandType.A_COMMAND);
            } else if(crrnt_cmd.matches(PATTERN_L)){
                System.out.println("L_COMMAND");
                return(CommandType.L_COMMAND);
            } else if(crrnt_cmd.matches(PATTERN_C)){
                System.out.println("C_COMMAND");
                return(CommandType.C_COMMAND);
            } else{
                return(CommandType.UNMATCH);
            }
        } else{
            return(null);
        }
    }

    public String symbol(){
        if(crrnt_cmd.matches(PATTERN_A)){
            return(crrnt_cmd.replace("@", ""));
        } else if(crrnt_cmd.matches(PATTERN_L)){
            return(crrnt_cmd.replace("(", "").replace(")", ""));
        }

        return(null);
    }

    public void splitCommand(){
        if(crrnt_cmd.matches(PATTERN_C)){
            String target = new String(crrnt_cmd);

            if(target.contains("=")){
                String[] splitted = target.split("=");
                this.dest = splitted[0];
                target = splitted[1];
            } else{
                this.dest = "null";
            }
            System.out.println("dest:" + this.dest);

            if(target.contains(";")){
                String[] splitted = target.split(";");
                this.comp = splitted[0];
                this.jump = splitted[1];
            } else{
                this.comp = target;
                this.jump = "null";
            }
            System.out.println("comp:" + this.comp);
            System.out.println("jump:" + this.jump);
        }
    }

    public String dest(){ return(this.dest); }
    public String comp(){ return(this.comp); }
    public String jump(){ return(this.jump); }

    public void reset(){
        try{
            this.buffReader.reset();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void closeFile(){
        try{
            if(buffReader != null){
                buffReader.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
