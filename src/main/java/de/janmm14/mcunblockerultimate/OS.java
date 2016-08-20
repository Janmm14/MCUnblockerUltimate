package de.janmm14.mcunblockerultimate;

import lombok.Getter;

public enum OS
{
    WINDOWS("win"),
    OSX("mac"),
    LINUX("linux", "unix"),
    UNKNOWN;

    private final String[] possibleNames;

    OS(final String... possibleNames) {
        this.possibleNames = (possibleNames);
    }

    @Getter
    private static final OS currentPlatform = calculateCurrentPlatform();

    public static OS calculateCurrentPlatform() {
        String name = System.getProperty("os.name").toLowerCase();
        for (OS os : values()) {
            for (String possName : os.possibleNames) {
                if (name.contains(possName)) {
                    return os;
                }
            }
        }
        return OS.UNKNOWN;
    }
}
