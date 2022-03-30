public enum CommandType {
    A_COMMAND(0),
    C_COMMAND(1),
    L_COMMAND(2),
    UNMATCH(3);

    private final int id;

    private CommandType(final int id){
        this.id = id;
    }

    public int getId(){
        return(this.id);
    }
}
