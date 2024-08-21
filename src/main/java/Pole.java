import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pole {
    private String name;
    private List<Question> questions;
    private boolean isReady;
    private int timeToWaitBeforePublish;
    private LocalDateTime timeSentOut;

    public Pole(String name) {
        this.name = name;
        isReady = false;
        this.questions = new ArrayList<>();
    }

    public LocalDateTime getTimeSentOut() {
        return timeSentOut;
    }

    public void setTimeSentOut(LocalDateTime timeSentOut) {
        this.timeSentOut = timeSentOut;
    }

    public void setTimeToWaitBeforePublish(int timeToWaitBeforePublish) {
        this.timeToWaitBeforePublish = timeToWaitBeforePublish;
    }

    public long getTimeToWaitBeforePublish() {
        return timeToWaitBeforePublish;
    }

    public  Question getLastQuestion(){
        return this.questions.getLast();
    }


    public String getName() {
        return name;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }
    public boolean isReady(){
        return this.isReady;
    }

    public void changeReadynes(){
        this.isReady=!isReady;
    }

    @Override
    public String toString() {
        return  name + ' ' +
                 questions +
                "isReady " + isReady +
                "timeToWaitBeforePublish " + timeToWaitBeforePublish +
                "timeSentOut " + timeSentOut ;
    }
}
