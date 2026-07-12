package dev.prchl.donututilities.module;

public final class PopupSettings {
    public enum Position {
        TOP_LEFT("TOP LEFT"),
        TOP_CENTER("TOP CENTER"),
        TOP_RIGHT("TOP RIGHT"),
        BOTTOM_RIGHT("BOTTOM RIGHT"),
        BOTTOM_CENTER("BOTTOM CENTER"),
        BOTTOM_LEFT("BOTTOM LEFT");

        private final String displayName;

        Position(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private Position position = Position.TOP_CENTER;

    public Position position() {
        return position;
    }

    public void cyclePosition() {
        Position[] values = Position.values();
        position = values[(position.ordinal() + 1) % values.length];
    }

    public void setPosition(Position position) {
        if (position != null) {
            this.position = position;
        }
    }
}
