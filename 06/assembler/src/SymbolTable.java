import java.util.HashMap;

public class SymbolTable{
    private HashMap<String, Integer> table;
    private static final String[] label = {
        "SP", "LCL", "ARG", "THIS", "THAT",
        "R0", "R1", "R2", "R3", "R4", "R5",
        "R6", "R7", "R8", "R9", "R10", "R11",
        "R12", "R13", "R14", "R15",
        "SCREEN", "KBD"
    };
    private static final int[] RAMaddress = {
        0, 1, 2, 3, 4,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15,
        16384, 24576
    };

    public SymbolTable(){
        this.table = new HashMap<>();
        for(int i=0; i < SymbolTable.label.length; i++){
            this.table.put(label[i], RAMaddress[i]);
        }
    }

    public void addEntry(String symbol, Integer address){
        table.put(symbol, address);
    }

    public boolean contains(String symbol){
        return(table.containsKey(symbol));
    }

    public int getAddress(String symbol){
        return(table.get(symbol));
    }

    public void dumpTable(){
        int size = table.size();
        for(int i=0; i < size; i++){
            System.out.println(table.get(label[i]));
        }
    }

    // public static void main(String[] args) {
    //     SymbolTable table = new SymbolTable();
    //     table.dumpTable();
    // }
}