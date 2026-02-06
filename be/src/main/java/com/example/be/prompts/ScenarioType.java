package com.example.be.prompts;
// ScenarioType.java
public enum ScenarioType {
    INVEST("invest", "prompts/romance_scam_invest.txt"),
    ROMANCE("romance", "prompts/romance_scam_money.txt");

    private final String key;
    private final String filePath;

    ScenarioType(String key, String filePath) {
        this.key = key;
        this.filePath = filePath;
    }

    public static String getPath(String key) {
        for (ScenarioType type : values()) {
            if (type.key.equalsIgnoreCase(key)) return type.filePath;
        }
        return "prompts/default.txt"; // 기본값
    }
}