package nimons.entity.item;

public abstract class Item {

    private String id;
    private String name;
    private boolean portable;

    public Item() {}

    public Item(String id, String name, boolean portable) {
        this.id = id;
        this.name = name;
        this.portable = portable;
    }

    public String getId() { 
        return id; 
    }

    public void setId(String id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public boolean isPortable() { 
        return portable; 
    }

    public void setPortable(boolean portable) { 
        this.portable = portable; 
    }
}
