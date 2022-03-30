import java.util.HashMap;

public class Code {
    private HashMap<String, String> dest;
    private HashMap<String, String> comp;
    private HashMap<String, String> jump;

    public Code(){
        String[] asm_dest = {"null", "M", "D", "MD", "A", "AM", "AD", "AMD"};
        String[] hack_dest = {"000", "001", "010", "011", "100", "101", "110", "111"};
        this.dest = new HashMap<>();
        for(int i=0; i < asm_dest.length; i++){
            this.dest.put(asm_dest[i], hack_dest[i]);
        }

        String[] asm_comp = {"0", "1", "-1", "D", "A", "!D", "!A", "-D", "-A",
                            "D+1", "A+1", "D-1", "A-1", "D+A", "D-A", "A-D", "D&A", "D|A",
                            "M", "!M", "-M", "M+1", "M-1", "D+M", "D-M", "M-D", "D&M", "D|M"};
        String[] hack_comp = {
                /*  0  */ "0101010", /*  1  */ "0111111", /* -1  */ "0111010", /*  D  */ "0001100",
                /*  A  */ "0110000", /* !D  */ "0001101", /* !A  */ "0110001", /* -D  */ "0001111",
                /* -A  */ "0110011", /* D+1 */ "0011111", /* A+1 */ "0110111", /* D-1 */ "0001110",
                /* A-1 */ "0110010", /* D+A */ "0000010", /* D-A */ "0010011", /* A-D */ "0000111",
                /* D&A */ "0000000", /* D|A */ "0010101", /*  M  */ "1110000", /* !M  */ "1110001",
                /* -M  */ "1110011", /* M+1 */ "1110111", /* M-1 */ "1110010", /*D+M */  "1000010",
                /* D-M */ "1010011", /* M-D */ "1000111", /* D&M */ "1000000", /* D|M */ "1010101"};
        this.comp = new HashMap<>();
        for(int i=0; i < asm_comp.length; i++){
            this.comp.put(asm_comp[i], hack_comp[i]);
        }

        String[] asm_jump = {"null", "JGT", "JEQ", "JGE", "JLT", "JNE", "JLE", "JMP"};
        String[] hack_jump = {"000", "001", "010", "011", "100", "101", "110", "111"};
        this.jump = new HashMap<>();
        for(int i=0; i < asm_jump.length; i++){
            this.jump.put(asm_jump[i], hack_jump[i]);
        }
    }

    public String dest(String mnemonic){
        if(this.dest.containsKey(mnemonic)){
            return(this.dest.get(mnemonic));
        } else{
            System.out.println("fail to search");
            return(null);
        }
    }
    
    public String comp(String mnemonic){
        if(this.comp.containsKey(mnemonic)){
            return(this.comp.get(mnemonic));
        } else{
            System.out.println("fail to search");
            return(null);
        }
    }

    public String jump(String mnemonic){
        if(this.jump.containsKey(mnemonic)){
            return(this.jump.get(mnemonic));
        } else{
            System.out.println("fail to search");
            return(null);
        }
    }

}
