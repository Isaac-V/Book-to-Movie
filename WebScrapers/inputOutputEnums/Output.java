package inputOutputEnums;

public enum Output {
    ESTPRODCOSTS (15),
    BOXOFFICEREV (15),
    AWARDNOMS (5),
    RATING (10);

    private final int size;
    Output(int size) {
        this.size = size;
    }
    
    public int size() { return size; }
    
}
