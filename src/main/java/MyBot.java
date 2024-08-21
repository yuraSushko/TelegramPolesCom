import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
//TODO change answers kist to map with str keys as options and value as lost of users that asnserd this
public class MyBot extends TelegramLongPollingBot {
    private Map<Long,BotUser> mapIdToUser;
    private SendMessage messegeTosend=null;
    private BotUser poleCreator;
    private InlineKeyboardMarkup inlineKeyboardMarkup;
    private Pole pole;
    private Object lock;

    public MyBot(String botToken) {
        super(botToken);
        lock = new Object();
        mapIdToUser= new HashMap<>();
        sendOutCurrentPole();
    }


    private List<SendMessage> createPole(){
        List<SendMessage> pollFromQuestions = new ArrayList<>();
        pollFromQuestions.add(new SendMessage());
        pollFromQuestions.getLast().setText(pole.getName());
        for (int q=0; q<pole.getQuestions().size();q++){
            List<String> answerOptionsThisQ = pole.getQuestions().get(q).getAnswerOptions();
            List<List<InlineKeyboardButton>> buttons= new ArrayList<>();

            for(int a=0; a<answerOptionsThisQ.size();a++) {
                buttons.add(
                        createInlineKeyboardButton(answerOptionsThisQ.get(a),(q)+""+answerOptionsThisQ.get(a)));
            }
            pollFromQuestions.add(new SendMessage());
            pollFromQuestions.getLast().setText(pole.getQuestions().get(q).getQuestionStr());
            inlineKeyboardMarkup = new InlineKeyboardMarkup(buttons);
            pollFromQuestions.getLast().setReplyMarkup(inlineKeyboardMarkup);
        }
        return pollFromQuestions;
    }


    private void distributePole(){
        System.out.println("distributePole");
        List<SendMessage> pollFromQuestions = createPole();
        System.out.println("distributePole1");
        mapIdToUser.entrySet().stream()
                //////.filter(e->!e.getKey().equals(poleCreator.getId()))
                .forEach(e->{
                    System.out.println("distributePole2");
                    for(SendMessage sm : pollFromQuestions){
                        sm.setChatId( e.getKey() );
                        excSendingMessg(sm);
                        System.out.println("distributePole33");
                    }
                    e.getValue().setStage(Stage.ANSWERING_POLE);
                });
        System.out.println("distributePole4");
        pole.setTimeSentOut(LocalDateTime.now());
    }

    public void sendOutCurrentPole(){
        new Thread(()->{
            while (true){
                if( this.pole!=null && pole.isReady()){
                    System.out.println("Thread poleCreator" + poleCreator);
                    System.out.println("entered  sendOutCurrentPole");
                    sleep( (int)pole.getTimeToWaitBeforePublish()*Constants.MINUTE);
                    System.out.println("done sleep sendCurPole");
                    distributePole();
                    pole.changeReadynes(); //TODO
                }
                else{
                   // System.out.println("else of Thread ");
                    sleep(1);
                }
            }
        }).start();
    }


    private void sleep(int seconds){
        try {
            Thread.sleep(seconds*Constants.SECOND);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void excSendingMessg(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private List<InlineKeyboardButton> createInlineKeyboardButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }


    private synchronized BotUser registerUser(Long currId,User curr){
        BotUser cuurentInteructionUser=new BotUser(currId,curr.getFirstName() +" "+curr.getLastName());
        mapIdToUser.keySet().stream().forEach(key->{
                String text= "Hi,\nSay hello to " + mapIdToUser.get(key).getName()+ " who just joined us";
                messegeTosend = new SendMessage(String.valueOf(key), text);
                excSendingMessg(messegeTosend);
            });
        this.mapIdToUser.put(currId,cuurentInteructionUser);
        return cuurentInteructionUser;

    }

    private boolean saidHi(String text){
        return text.equalsIgnoreCase("Hi") || text.equals("היי") || text.equals("/start");
    }

    @Override
    public void onUpdateReceived(Update update) {
        String calBack="", text="" ; User curr=null;

        if (update.hasMessage() && update.getMessage().hasText()) {
            text = update.getMessage().getText();
            curr= update.getMessage().getFrom();
            System.out.println(text+ " text ");
        }
        else if(update.hasCallbackQuery()) {
            calBack = update.getCallbackQuery().getData();
            curr= update.getCallbackQuery().getFrom();
            System.out.println(calBack + " calBack");
        }
        Long currId = curr.getId();
        BotUser cuurentInteructionUser =
        mapIdToUser.get(currId)==null ? registerUser(currId,curr) : mapIdToUser.get(currId);
        System.out.println(cuurentInteructionUser + " after register");
        switch (cuurentInteructionUser.getStage()){
            case NEW:
                if(saidHi(text)){
                    if(poleCreator==null){
                        if(mapIdToUser.size()>=Constants.MIN_COMMUNITY_SIZE_TO_SEND_POLE ) {
                            List<List<InlineKeyboardButton>> buttons= new ArrayList<>();
                            buttons.add(createInlineKeyboardButton("Yes","YES"));
                            buttons.add(createInlineKeyboardButton("No","NO"));
                            inlineKeyboardMarkup = new InlineKeyboardMarkup(buttons);//(Arrays.asList(inlineKeyboardButtons));
                            messegeTosend = new SendMessage(String.valueOf(currId),"Would you like to create a pole?\n" );
                            messegeTosend.setReplyMarkup(inlineKeyboardMarkup);
                            cuurentInteructionUser.setStage(Stage.ASKED_TO_CREATE);
                            excSendingMessg(messegeTosend);
                        }
                    }
                }
                break;
            case ASKED_TO_CREATE:
                System.out.println("ASKED" + update.getCallbackQuery().getData());
                if (update.getCallbackQuery().getData().equals("YES")) {
                    cuurentInteructionUser.setStage(Stage.CREATING_POLE);
                    synchronized (lock){ poleCreator = cuurentInteructionUser;}
                    System.out.println(poleCreator + " created pole creator");
                    excSendingMessg(new SendMessage(String.valueOf(currId), "whats the name for the pole?"));

                } else {
                    excSendingMessg(new SendMessage(String.valueOf(currId), "alright maby next time"));
                    cuurentInteructionUser.setStage(Stage.NEW);
                }
                break;
            case CREATING_POLE:
               synchronized (lock){pole = new Pole(text);}
                excSendingMessg(new SendMessage(String.valueOf(currId), "add a question"));
                cuurentInteructionUser.setStage( Stage.ADD_QUESTION);
                break;

            case ADD_QUESTION:
                this.pole.addQuestion(new Question(text));
                excSendingMessg(new SendMessage(String.valueOf(currId), "add optional answer"));
                cuurentInteructionUser.setStage( Stage.ADD_ANSWER);
                break;

            case ADD_ANSWER:

                this.pole.getLastQuestion().addAnswerOption(text);
                if(this.pole.getQuestions().size()<4) {
                    List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                    if(this.pole.getLastQuestion().getAnswerOptions().size()>1) {
                        buttons.add(createInlineKeyboardButton("Add Question", "QUESTION"));
                        buttons.add(createInlineKeyboardButton("Done", "DONE"));

                    }
                    if(this.pole.getLastQuestion().getAnswerOptions().size()<4){
                        buttons.add(createInlineKeyboardButton("Add Answer", "ANSWER"));
                    }

                    inlineKeyboardMarkup = new InlineKeyboardMarkup(buttons);
                    messegeTosend = new SendMessage(String.valueOf(currId),
                            "Choose to add or end the pole");
                    messegeTosend.setReplyMarkup(inlineKeyboardMarkup);
                    cuurentInteructionUser.setStage(Stage.ADD_Q_OR_A);
                }
                excSendingMessg(messegeTosend);
                break;

            case ADD_Q_OR_A:
                switch (calBack){
                    case "QUESTION":
                        cuurentInteructionUser.setStage(Stage.ADD_QUESTION);
                        excSendingMessg(new SendMessage(String.valueOf(currId),"whats your question"));
                        break;
                    case "ANSWER":
                        cuurentInteructionUser.setStage(Stage.ADD_ANSWER);
                        excSendingMessg(new SendMessage(String.valueOf(currId),"whats your optional answer"));
                        break;
                    case "DONE":
                        excSendingMessg(new SendMessage(String.valueOf(currId),"how much delay in minutes"));
                        cuurentInteructionUser.setStage(Stage.DELAY_TIME);
                        break;

                }
                break;

            case DELAY_TIME:
                String delayTimeStr="";
                for (int i = 0; i < text.length(); i++) {
                    if(Character.isDigit(text.charAt(i)) /*|| (text.charAt(i)=='.')*/ ){
                        System.out.println(delayTimeStr);
                        delayTimeStr+=text.charAt(i);
                        System.out.println(delayTimeStr);
                    }
                }
                double delayTime =Integer.parseInt(delayTimeStr);
                System.out.println(delayTime +" delayTime");
               synchronized (lock) {
                   pole.setTimeToWaitBeforePublish((int) (delayTime ));
                   System.out.println(pole.getTimeToWaitBeforePublish() + " get");
                   pole.changeReadynes();
                   System.out.println(pole.isReady());
                   cuurentInteructionUser.setStage(Stage.SENDING_OUT_POLE);
                   System.out.println(cuurentInteructionUser);
               }

                break;

            case ANSWERING_POLE:
                System.out.println("ANSWERING_POLE");
                System.out.println("  all Q"  + pole.getQuestions() );
                String answerToadd=null;int questionNum=-1;
                try {
                    questionNum = Integer.valueOf(calBack.charAt(0) + "");
                    System.out.println(questionNum + " questionNum ");
                    System.out.println(calBack);
                /*for (int i = 0; i < calBack.length(); i++) {
                    System.out.print( calBack.charAt(i)+", ");;
                }
                System.out.println();*/
                    answerToadd = calBack.substring(1);//charAt(1)+"";
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                System.out.println(answerToadd  + " answerToadd");
                if(questionNum>=0) {
                    pole.getQuestions()
                            .get(questionNum)
                            .addUserAnswerToQuestion(answerToadd);
                    System.out.println("ANSWERING_POLE after add");
                    EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
                    editMessageReplyMarkup.setChatId(String.valueOf(currId));
                    editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

                    List<List<InlineKeyboardButton>> buttonss= new ArrayList<>();
                    for (String ansOp : pole.getQuestions().get(questionNum).getAnswerOptions()) {
                        buttonss.add(createInlineKeyboardButton(ansOp, "-1"));
                    }
                    //buttonss.add(createInlineKeyboardButton(calBack.substring(1), null));
                    System.out.println(buttonss);
                    inlineKeyboardMarkup = new InlineKeyboardMarkup(buttonss);

                    editMessageReplyMarkup.setReplyMarkup(/*null*/inlineKeyboardMarkup); //TODO create KEYBIORAD no calback from the answer provided
                    System.out.println(editMessageReplyMarkup);
                    try {
                        execute(editMessageReplyMarkup);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("drropped keybouard");
                }

                if(pole.getQuestions().stream()
                       .filter(q -> mapIdToUser.size()-1 != q.getAllAnswersFromUsers().size())
                       .findAny().orElse(null) == null
                   || Duration.between(pole.getTimeSentOut(), LocalDateTime.now()).toMinutes()>=5){

                    mapIdToUser.entrySet().stream()
                    .forEach(e-> e.getValue().setStage(Stage.NEW));

                    sendStatisticsToPoleCreator();
                }

                sendStatisticsToPoleCreator(); //TODO remove

                break;
        }

    }


    public void sendStatisticsToPoleCreator(){
        //TODO
        String msg="Here are the results for your pole:\n";
        Map<String,Map<String,Long> > map = pole.getQuestions()
                .stream()
                .collect(Collectors
                .toMap(
                    Question::getQuestionStr,
                    q->q.getAllAnswersFromUsers()
                        .stream().collect(
                        Collectors.groupingBy(
                        String::valueOf,
                        Collectors.counting()  )
                        )
                      )
                );
        System.out.println(map);
        for (Map.Entry<String,Map<String,Long> > entry : map.entrySet()){
            msg+=entry.getKey()+"\n";
            long sum=0;
            for (Map.Entry<String,Long> inMap : entry.getValue().entrySet()){
                sum += inMap.getValue();
            }
            for (Map.Entry<String,Long> inMap : entry.getValue().entrySet()){
                msg += "  "+inMap.getKey() + ": "+  inMap.getValue()*100/sum +"%\n";
            }
        }
        messegeTosend = new SendMessage(String.valueOf(poleCreator.getId()),msg);
        excSendingMessg(messegeTosend);
        poleCreator=null;
        pole= null;
    }

    @Override
    public String getBotUsername() {
        return "PoleComYSS_bot";
    }
}
