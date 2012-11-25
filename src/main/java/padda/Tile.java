package padda;

public class Tile {

    public int type_;
    public float z_;
    public float height_;
    
    
    public Tile(int type, float z) {
        type_ = type;
        z_ = z;
        switch (type_) {
            case 0:
                height_ = 0.0f;
                break;
            case 1:
                height_ = 20.0f;
                break;
            default:
                height_ = 0.0f;
        }
    }
    
    public float top() {
        return z_ + height_;
    }

}
