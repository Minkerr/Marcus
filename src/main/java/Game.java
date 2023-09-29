public class Game{
    public String PlayerId1 = "";
    public String PlayerId2 = "";
    public String PlayerName1 = "";
    public String PlayerName2 = "";
    public int turn = 1;
    public String Turn1 = "";
    public String Turn2 = "";
    private Bot bot;
    public int gameNumber = 1;
    public int targetNumber = -1;

    public Game(Bot bot, String Id1, String Id2, String Name1, String Name2){  // конструктор игры
        this.PlayerId1 = Id1;
        this.PlayerId2 = Id2;
        this.PlayerName1 = Name1;
        this.PlayerName2 = Name2;
        this.bot = bot;
    }

    public boolean correctTurn(String s){ //проверка корректности хода
        int correct1 = 1, correct2 = 0; // проверка1, проверка2
        for(int i = 0; i < s.length(); i++) {
            if (!(s.charAt(i) <= '9' && s.charAt(i) >= '0')) correct1 = 0; //фигня проверяет что ход игрока - число
        }
        if(correct1 == 1) {
            int turnNumber = bot.stringToInt(s);
            for (int i = 1; i < gameNumber; i++) { //фигня проверяет, что число игрока - делитель
                if (gameNumber % i == 0) {
                    if (i == turnNumber) correct2 = 1;
                }
            }
        }
        return (correct1 == 1 && correct2 == 1);
    }

    public void clearGameboard(){ // очищение игровой доски после окончания игры
        PlayerId1 = "";
        PlayerId2 = "";
        PlayerName1 = "";
        PlayerName2 = "";
        turn = 1;
        String Turn1 = "";
        String Turn2 = "";
        gameNumber = 1;
        targetNumber = -1;
    }

    public void reply(String playerTurn){
        gameNumber += bot.stringToInt(playerTurn);
        if(turn == 1){
            if(gameNumber >= targetNumber){
                bot.sendAnswer("Поражение", PlayerId1);
                bot.sendAnswer("Победа!", PlayerId2);
                clearGameboard();
            }
            else {
                bot.sendAnswer("Текущее число: " + gameNumber, PlayerId1);
                bot.sendAnswer("Текущее число: " + gameNumber, PlayerId2);
                bot.sendAnswer("Ход противника", PlayerId1);
                bot.sendAnswer("Ваш ход", PlayerId2);
            }
        }
        else{
            if(gameNumber >= targetNumber){
                bot.sendAnswer("Победа!", PlayerId1);
                bot.sendAnswer("Поражение", PlayerId2);
                clearGameboard();
            }
            else{
                bot.sendAnswer("Текущее число: " + gameNumber, PlayerId1);
                bot.sendAnswer("Текущее число: " + gameNumber, PlayerId2);
                bot.sendAnswer("Ход противника", PlayerId2);
                bot.sendAnswer("Ваш ход", PlayerId1);
            }
        }
    }
}
