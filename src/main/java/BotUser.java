import java.util.ArrayList;
import java.util.List;

public class BotUser {
    private  Long id;
    private  String name;
    private Stage stage;
    //private List<String> logger;

    public BotUser(Long id, String name) {
        this.id = id;
        this.name = name;
        this.stage = Stage.NEW;
        //this.logger=new ArrayList<>();
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
/*public List<String> getLogger() {
        return logger;
    }

    public void addToLoger(String point) {
        this.logger.add(point);
    }
    public void truncateLoger() {
        this.logger.clear();
    }*/

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return  id + " " +
                 name +" " + stage
                ;
    }
}
