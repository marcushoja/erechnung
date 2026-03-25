package de.hojam2.erechnung.model;

public enum LineUnit {
    STUECK("Stk", "C62"),
    STUNDEN("Stunden", "HUR"),
    TAG("Tag", "DAY"),
    PAUSCHALE("Pauschale", "ZZ");

    private final String label;
    private final String code;

    LineUnit(String label, String code) {
        this.label = label;
        this.code = code;
    }

    public String label() {
        return label;
    }

    public String code() {
        return code;
    }

    public static LineUnit fromCode(String code) {
        if (code == null) {
            return STUECK;
        }
        for (LineUnit unit : values()) {
            if (unit.code.equalsIgnoreCase(code)) {
                return unit;
            }
        }
        return STUECK;
    }
}
