import java.util.ArrayList;
import java.util.List;

public class Question {
    private String questionStr;
    private List<String> answerOptions;
    private List<String> allAnswersFromUsers;
//TODO change answers kist to map with str keys as options and value as lost of users that asnserd this


    public Question(String questionStr) {
        this.questionStr = questionStr;
        this.answerOptions = new ArrayList<>();
        this.allAnswersFromUsers= new ArrayList<>();
    }

    public String getQuestionStr() {
        return questionStr;
    }

    //public void setQuestionStr(String questionStr) {
    //    this.questionStr = questionStr;
   // }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }

    //public void setAnswerOptions(List<String> answerOptions) {
    //    this.answerOptions = answerOptions;
   // }

    public void addAnswerOption(String ansOp){
        this.answerOptions.add(ansOp);
    }

    public List<String> getAllAnswersFromUsers() {
        return allAnswersFromUsers;
    }

    public void addUserAnswerToQuestion(String ans) {
        this.allAnswersFromUsers.add(ans) ;
    }

    @Override
    public String toString() {
        return questionStr +
                " " + answerOptions +
                " " + allAnswersFromUsers ;

    }
}
