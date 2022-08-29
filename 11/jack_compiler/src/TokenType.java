public enum TokenType {
    KEYWORD(0),
    SYMBOL(1),
    INT_CONST(2),
    STRING_CONST(3),
    IDENTIFIER(4);

    private final int id;

    private TokenType(final int id){
        this.id = id;
    }

    public int getId(){
        return(this.id);
    }
}
