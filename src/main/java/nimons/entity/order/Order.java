package nimons.entity.order;

public class Order {

    private int index;
    private Recipe recipe;
    private int reward;
    private int penalty;
    private int timeLimitSeconds;
    private double remainingTimeSeconds;
    private OrderStatus status;
    
    
    private float opacity = 0.0f; 
    private boolean isFadingIn = true;
    private boolean isFadingOut = false;
    private static final float FADE_SPEED = 0.05f; 

    public Order() {}

    public Order(int index, Recipe recipe, int reward, int penalty, int timeLimitSeconds,
                 double remainingTimeSeconds, OrderStatus status) {
        this.index = index;
        this.recipe = recipe;
        this.reward = reward;
        this.penalty = penalty;
        this.timeLimitSeconds = timeLimitSeconds;
        this.remainingTimeSeconds = remainingTimeSeconds;
        this.status = status;
    }

    
    public int getIndex() { 
        return index; 
    }

    public void setIndex(int index) { 
        this.index = index; 
    }

    public Recipe getRecipe() { 
        return recipe; 
    }

    public void setRecipe(Recipe recipe) { 
        this.recipe = recipe; 
    }

    public int getReward() { 
        return reward; 
    }

    public void setReward(int reward) { 
        this.reward = reward; 
    }

    public int getPenalty() { 
        return penalty; 
    }

    public void setPenalty(int penalty) { 
        this.penalty = penalty; 
    }

    public int getTimeLimitSeconds() { 
        return timeLimitSeconds; 
    }

    public void setTimeLimitSeconds(int timeLimitSeconds) { 
        this.timeLimitSeconds = timeLimitSeconds; 
    }

    public double getRemainingTimeSeconds() { 
        return remainingTimeSeconds; 
    }

    public void setRemainingTimeSeconds(double remainingTimeSeconds) { 
        this.remainingTimeSeconds = remainingTimeSeconds; 
    }

    public OrderStatus getStatus() { 
        return status; 
    }

    public void setStatus(OrderStatus status) { 
        this.status = status;
        
        if (status == OrderStatus.COMPLETED || status == OrderStatus.FAILED) {
            isFadingOut = true;
            isFadingIn = false;
        }
    }
    
    
        
    public void updateAnimation() {
        if (isFadingIn && opacity < 1.0f) {
            opacity += FADE_SPEED;
            if (opacity >= 1.0f) {
                opacity = 1.0f;
                isFadingIn = false;
            }
        } else if (isFadingOut && opacity > 0.0f) {
            opacity -= FADE_SPEED;
            if (opacity <= 0.0f) {
                opacity = 0.0f;
            }
        }
    }
    
    public float getOpacity() {
        return opacity;
    }
    
        
    public boolean isFullyFaded() {
        return isFadingOut && opacity <= 0.0f;
    }
}
