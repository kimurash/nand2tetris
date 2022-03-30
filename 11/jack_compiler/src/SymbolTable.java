import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, Symbol> classTable;
    private HashMap<String, Symbol> subroutineTable;

    private int numStatic = 0;
    private int numField = 0;
    private int numArg = 0;
    private int numVar = 0;

    public SymbolTable(){
        this.classTable = new HashMap<>();
        this.subroutineTable = new HashMap<>();
    }

    public void startSubroutine(){
        this.subroutineTable.clear();
        this.numArg = 0;
        this.numVar = 0;
    }

    public void define(String name, String type, String kind){
        switch(kind){
            case "static" :
                classTable.put(name, new Symbol(type, kind, this.numStatic++));
                break;
            case "field" :
                classTable.put(name, new Symbol(type, kind, this.numField++));
                break;
            case "arg" :
                subroutineTable.put(name, new Symbol(type, kind, this.numArg++));
                break;
            case "var" :
                subroutineTable.put(name, new Symbol(type, kind, this.numVar++));
                break;
            default : break;
        }
    }

    public int varCount(String kind){
        switch(kind){
            case "static" : return(numStatic);
            case "field" : return(numField);
            case "arg" : return(numArg);
            case "var" : return(numVar);
            default : 
                System.out.println("varCount: invalid kind of " + kind );
                return(0);
        }
    }

    public String kindOf(String name){
        if(classTable.containsKey(name)){
            return(classTable.get(name).getKind());
        } else if(subroutineTable.containsKey(name)){
            return(subroutineTable.get(name).getKind());
        } else{
            return(null);
        }
    }

    public String typeOf(String name){
        if(classTable.containsKey(name)){
            return(classTable.get(name).getType());
        } else if(subroutineTable.containsKey(name)){
            return(subroutineTable.get(name).getType());
        } else{
            return(null);
        }
    }

    public int indexOf(String name){
        if(classTable.containsKey(name)){
            return(classTable.get(name).getIndex());
        } else if(subroutineTable.containsKey(name)){
            return(subroutineTable.get(name).getIndex());
        } else{
            return(-1);
        }
    }

    public void dumpSubroutineTable(){
        System.out.println("name\ttype\tkind\tindex");
        for(String name : subroutineTable.keySet()){
            Symbol symbol = subroutineTable.get(name);
            System.out.println(name + "\t" + symbol.getType() + "\t" + symbol.getKind() + "\t" + symbol.getIndex());
        }
    }

    public void dumpClassTable(){
        System.out.println("name\ttype\tkind\tindex");
        for(String name : classTable.keySet()){
            Symbol symbol = classTable.get(name);
            System.out.println(name + "\t" + symbol.getType() + "\t" + symbol.getKind() + "\t" + symbol.getIndex());
        }
    }
}
