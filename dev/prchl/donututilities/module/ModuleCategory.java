package dev.prchl.donututilities.module;

public enum ModuleCategory {
    MISC("MISC"),
    DONUT("DONUT"),
    BASE("BASEFINDING"),
    RENDER("RENDER"),
    CLIENT("CLIENT");

    private final String title;

    ModuleCategory(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
