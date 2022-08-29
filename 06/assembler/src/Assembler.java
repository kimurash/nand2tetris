import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Assembler {
    public String dec2bin(int dec){
        String str = Integer.toString(dec);
        return(this.dec2bin(str));
    }

    public String dec2bin(String dec){
        int num = Integer.parseInt(dec);

        StringBuilder builder = new StringBuilder("");
        for(int i=0; i < 15; i++){
            if(num <= 0){
                builder.append("0");
                continue;
            }

            if((num % 2) == 0){
                builder.append("0");
            } else{
                builder.append("1");
            }
            num = num / 2;
        }
        builder.reverse();
        return(builder.toString());
    }

    public static void main(String[] args) {
        Assembler assembler = new Assembler();
        Parser parser = new Parser("C:\\Users\\Shunsei\\nand2tetris\\projects\\06\\" + args[0] +  "\\" + args[1] + ".asm");
        Code code = new Code();
        SymbolTable table = new SymbolTable();

        int nline = 0;
        while(parser.hasMoreCommands()){
            // parser.advance();
            CommandType type = parser.commandType();
            switch(type.getId()){
                case 0 : // A_COMMAND
                case 1 : // C_COMMAND
                    nline++;
                    break;
                case 2 : // L_COMMAND
                    table.addEntry(parser.symbol(), nline);
                    break;
                case 3 : // UNMATCH
                    System.out.println("Nothing to mach");
                    break;
                default: break;
            }
        }

        parser.reset();
        // parser.closeFile();
        // parser = new Parser("C:\\Users\\Shunsei\\nand2tetris\\projects\\06\\" + args[0] +  "\\" + args[1] + ".asm");

        try(
            BufferedWriter buffwriter = new BufferedWriter(new FileWriter(args[1] + ".hack"));
        ){
            StringBuilder builder = new StringBuilder("");
            String binary = null;
            int naddr = 16;
            while(parser.hasMoreCommands()){
                // parser.advance();
                CommandType type = parser.commandType();
                switch(type.getId()){
                    case 0 : // A_COMMAND
                        builder.append("0");
                        String symbol = parser.symbol();
                        if(parser.symbol().matches("\\d+")){
                            builder.append(assembler.dec2bin(symbol));
                        } else{
                            if(table.contains(symbol)){
                                builder.append(assembler.dec2bin(table.getAddress(symbol)));
                            } else{
                                table.addEntry(symbol, naddr);
                                builder.append(assembler.dec2bin(naddr));
                                naddr++;
                            }
                        }
                        break;
                    case 1 : // C_COMMAND
                        builder.append("111");
                        parser.splitCommand();
                        builder.append(code.comp(parser.comp()));
                        builder.append(code.dest(parser.dest()));
                        builder.append(code.jump(parser.jump()));
                        break;
                    case 2 : // L_COMMAND
                        System.out.println();
                        continue;
                        // break;
                    default: break;
                }
                binary = builder.toString();
                builder.delete(0, (builder.length()));
                buffwriter.write(binary);
                buffwriter.newLine();

                System.out.println();
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        parser.closeFile();   
    }
}
