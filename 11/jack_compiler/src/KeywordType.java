public enum KeywordType {
    CLASS(0),
    CONSTRUCTOR(1),
    FUNCTION(2),
    METHOD(3),
    FIELD(4),
    STATIC(5),
    VAR(6),
    INT(7),
    CHAR(8),
    BOOLEAN(9),
    VOID(10),
    TRUE(11),
    FALSE(12),
    NULL(13),
    THIS(14),
    LET(15),
    DO(16),
    IF(17),
    ELSE(18),
    WHILE(19),
    RETURN(20);

    private final int id;

    private KeywordType(final int id){
        this.id = id;
    }

    public int getId(){
        return(this.id);
    }

    public static void main(String[] args) {
        System.out.println(KeywordType.BOOLEAN.toString().toLowerCase());
    }
}
