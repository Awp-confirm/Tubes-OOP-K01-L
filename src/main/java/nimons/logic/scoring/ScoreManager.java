package nimons.logic.scoring;

public class ScoreManager {
    private static int score = 0;

    public static void addScore(int amount) {
        score += amount;
        System.out.println("[GAME] Score bertambah! Total: " + score);
    }

    public static void reduceScore(int amount) {
        score -= amount;
        System.out.println("[GAME] Penalti! Total: " + score);
    }

    public static int getScore() {
        return score;
    }
}