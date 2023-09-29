import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;


public class Bot extends TelegramLongPollingBot {
    //https://github.com/rubenlagus/TelegramBots/wiki/Getting-Started
    //https://habr.com/ru/post/418905/
    //https://ru.stackoverflow.com/questions/715154
    //1078618715 -
    public String WaitingPlayerId = "";
    public String WaitingPlayerName = "";
    Game game = new Game(this,"", "", "", "");
    GameAI gameAi = new GameAI(this,"");
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage(); //update.getMessage - метод возвращающий сообщение полученное ботом, message - это сообщение с которым мы работаем
        if (update.hasMessage() && message.hasText()) {
            String messageText = message.getText(); // текст сообщения
            if(messageText.equals("/start")){
                sendAnswer(message, "Добро пожаловать!");
            }
            else if(messageText.equals("Правила")){
                String rules = "Правила игры.\n" +
                    "В свой ход игрок прибавляет к числу его делитель, исключая само число. " +
                    "Игра начинается с 2. Игрок первый набравший число, определяемое случайно в начале игры, проигрывает.";
                sendAnswer(message, rules);
            }
            else if(messageText.equals("Играть c компьютером")){
                if(gameAi.PlayerId.equals("")) {
                    System.out.println(message.getChatId());
                    PrintWriter pw = null; //logs
                    try {
                        pw = new PrintWriter(new FileWriter("logs.txt", true));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    pw.println(message.getChatId() + " " + message.getChat().getFirstName());
                    pw.close();//logs
                    gameAi.PlayerId = message.getChatId().toString();
                    sendAnswer("Игра началась!", gameAi.PlayerId);
                    gameAi.targetNumber = 64 + (int) (Math.random() * 64);
                    sendAnswer("Проигрывает игрок, который достигнет числа " + gameAi.targetNumber + " или большего", gameAi.PlayerId);
                    gameAi.gameNumber = 2;
                    sendAnswer("Текущее число: " + gameAi.gameNumber, gameAi.PlayerId);
                    sendAnswer("Ваш ход", gameAi.PlayerId);
                    gameAi.winTurn();
                    gameAi.winTurn1();
                }
                else{
                    sendAnswer(message,"Все сервера переполнены. Зайдите позже");
                }
            }
            else if(messageText.equals("Играть")){
                if(game.PlayerId1.equals("")) {
                    System.out.println(message.getChatId());
                    sendAnswer(message, "Ожидание противника...");
                    if (WaitingPlayerId.equals("")) { //первый залетевший игрок становится ожидающим
                        WaitingPlayerId = message.getChatId().toString();
                        WaitingPlayerName = message.getChat().getFirstName().toString();
                    } else if (!WaitingPlayerId.equals(message.getChatId().toString())) {
                        game.PlayerId1 = WaitingPlayerId; //когда залетает второй, начинается игра. Для неё заполняются соответсвующие данные
                        game.PlayerId2 = message.getChatId().toString();
                        game.PlayerName1 = WaitingPlayerName;
                        game.PlayerName2 = message.getChat().getFirstName().toString();
                        game.turn = 1;
                        sendAnswer("Игра началась!", game.PlayerId1);
                        sendAnswer("Игра началась!", game.PlayerId2);
                        game.targetNumber = 64 + (int) (Math.random() * 64);
                        game.gameNumber = 2;
                        sendAnswer("Проигрывает игрок, который достигнет числа " + game.targetNumber + " или большего", game.PlayerId1);
                        sendAnswer("Проигрывает игрок, который достигнет числа " + game.targetNumber + " или большего", game.PlayerId2);
                        sendAnswer("Текущее число: " + game.gameNumber, game.PlayerId1);
                        sendAnswer("Ход противника", game.PlayerId2);
                        WaitingPlayerId = "";  // обнуление строки с ожидающим игроком
                        WaitingPlayerName = "";//
                    }
                }
                else{
                    sendAnswer(message,"Все сервера переполнены. Зайдите позже");
                }
            }
            else if(messageText.charAt(0) == '!' && message.getChatId().toString().equals("1078618715")){
                System.out.println(messageText.substring(1));
                sendAnswer(messageText.substring(1), ""+540354738);
            }
            else{
                if(game.PlayerId1.equals(message.getChatId().toString()) && game.turn == 1){
                    game.Turn1 = messageText;
                    if(game.correctTurn(game.Turn1)){
                        game.reply(game.Turn1);
                        game.turn = 2;
                    }
                    else{
                        sendAnswer("Некорректный ход", game.PlayerId1);
                    }
                }
                else if(game.PlayerId2.equals(message.getChatId().toString()) && game.turn == 2){
                    game.Turn2 = messageText;
                    if(game.correctTurn(game.Turn2)) {
                        game.reply(game.Turn2);
                        game.turn = 1;
                    }
                    else{
                        sendAnswer("Некорректный ход", game.PlayerId2);
                    }
                }
                else if(gameAi.PlayerId.equals(message.getChatId().toString())){
                    gameAi.Turn = messageText;
                    if(gameAi.correctTurn(gameAi.Turn)){
                        gameAi.gameNumber += stringToInt(gameAi.Turn);
                        try {                      ///это
                            gameAi.reply();          //ответ на ход
                        } catch (IOException e) {  ///так
                            e.printStackTrace();   ///надо
                        }
                    }
                    else{
                        sendAnswer("Некорректный ход", gameAi.PlayerId);
                    }
                }
                else if(!game.PlayerId2.equals(message.getChatId().toString()) && !game.PlayerId1.equals(message.getChatId().toString())){
                    sendAnswer(message, "Неизвестная команда");
                    System.out.println(message.getChatId().toString());
                }
            }
        }
    }

    public String getBotUsername() {
        return "MarcusGameBot";
    }

    @Override

    public String getBotToken() {
        return "ENTER THE TOKEN";
    }

    public void sendAnswer(Message message, String text){ //Метод откликающийся на любое сообщение
        SendMessage sendMessage = new SendMessage(); // Create a SendMessage object with mandatory fields, этот объект - сообщение которое отправит бот
        sendMessage.setChatId(message.getChatId().toString());//Задаём  chatId куда отправлять сообщение
        sendMessage.setText(text);//Задаём текст сообщения
        if(text.equals("Добро пожаловать!") || text.equals("Неизвестная команда")) setStartKeyboard(sendMessage);
        try {
            execute(sendMessage); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendAnswer(String text, String ChatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(ChatId);
        sendMessage.setText(text);
        int x = Math.max(game.gameNumber, gameAi.gameNumber);
        if(text.equals("Ваш ход")) setGameButtons(sendMessage, x);
        if(text.equals("Игра началась!")) setGameButtons(sendMessage, 2);
        if(text.equals("Поражение") || text.equals("Победа!")) setStartKeyboard(sendMessage);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public int stringToInt(String s){ //вспомогательная функция
        int res = 0;
        for(int i = 0; i < s.length(); i++){
            res *= 10;
            res += (s.charAt(i) - '0');
        }
        return res;
    }

    public void setGameButtons(SendMessage sendMessage, int turnNumber) {
        ReplyKeyboardMarkup gameKeyboard = new ReplyKeyboardMarkup(); // наша клава //✂️📄🪨
        sendMessage.setReplyMarkup(gameKeyboard);
        gameKeyboard.setSelective(true);
        gameKeyboard.setResizeKeyboard(true);
        gameKeyboard.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();//список слоёв кнопок
        //слой кнопок ------ KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow [] keyboardRows = new KeyboardRow[5];

        for(int i = 0; i < 5; i++){
            keyboardRows[i] = new KeyboardRow();
        }

        //keyboardRows[0].add("Цель");

        int count = gameAi.d(turnNumber); // колво выводимых чисел
        int[] dlist = new int[count];//
        int counter = 0;
        for (int i = 1; i < turnNumber; i++) { //фигня делает массив делителей
            if (turnNumber % i == 0) {
                dlist[counter] = i;
                counter++;
            }
        }
        counter = 0;
        for(int i = 1; i < 5; i++){
            for(int j = 0; j < 4; j++){
                if(counter >= count) break;
                keyboardRows[i].add(dlist[counter] + "");
                counter++;
            }
        }

        for(int i = 0; i < 5; i++){
            keyboard.add(keyboardRows[i]);//закидваем слои в список строк
        }
        gameKeyboard.setKeyboard(keyboard); //делаем нашу клаву короче
    }

    public void setStartKeyboard(SendMessage sendMessage) {
        ReplyKeyboardMarkup startKeyboard = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(startKeyboard);
        startKeyboard.setSelective(true);
        startKeyboard.setResizeKeyboard(true);
        startKeyboard.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton("Играть"));
        keyboardRow1.add(new KeyboardButton("Играть c компьютером"));
        keyboardRow2.add(new KeyboardButton("Правила"));
        keyboard.add(keyboardRow1);
        keyboard.add(keyboardRow2);
        startKeyboard.setKeyboard(keyboard);
    }
}