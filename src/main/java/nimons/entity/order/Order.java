package nimons.entity.order;

public class Order {

    private int index;
    private Recipe recipe;
    private int reward;
    private int penalty;
    private int timeLimitSeconds;
    private double remainingTimeSeconds;
    private OrderStatus status;

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

    // getters & setters
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
    }
}
