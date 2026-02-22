package cellar.fixture.java;

/**
 * A Java enum for testing enum symbol extraction.
 */
public enum CellarJavaEnum {
    ALPHA("alpha"),
    BETA("beta"),
    GAMMA("gamma");

    private final String label;

    CellarJavaEnum(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isFirst() {
        return this == ALPHA;
    }
}
