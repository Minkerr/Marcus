import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GameAI {
    public String PlayerId = "";
    public int gameNumber = 1;
    public int targetNumber = -1;
    private Bot bot;
    public String Turn = "";
    private int [] winTurns = new int[32];
    private int [] winTurns1 = new int[64];
    private int winTurnCount = 0;
    private int winTurnCount1 = 0;

    public GameAI(Bot bot, String Id){  // конструктор игры
        this.PlayerId = Id;
        this.bot = bot;
    }

    public int d(int x){ // count of deliteley
        int number = x, count = 0;
        for(int i = 1; i < number; i++){
            if(number % i == 0){
                count++;
            }
        }
        return count;
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

    public void winTurn(){ //просчитывает из которых можно загнать в проигрышную позицию
        for(int j = 2 * targetNumber / 3 - 1; j < targetNumber; j++){
            int count = d(j); // колво делителей
            int[] dlist = new int[count];//
            int counter = 0;
            for (int i = 1; i < j; i++) { //фигня делает массив делителей
                if (j % i == 0) {
                    dlist[counter] = i;
                    counter++;
                }
            }
            for(int i = 0; i < count; i++){
                if(j+dlist[i] == targetNumber - 1){
                    winTurns[winTurnCount] = j;
                    winTurnCount++;
                }
            }
        }
    }

    public void winTurn1(){
        for(int j = 2; j < targetNumber; j++) {
            int count = d(j); // колво делителей
            int[] dlist = new int[count];//
            int counter = 0;
            for (int i = 1; i < j; i++) { //фигня делает массив делителей
                if (j % i == 0) {
                    dlist[counter] = i;
                    counter++;
                }
            }
            int flagin = 0, flag = 1;
            for (int i = 0; i < count; i++) {
                for (int k = 0; k < winTurnCount; k++) {
                    if (j + dlist[i] == winTurns[k] || j + dlist[i] >= targetNumber) {
                        flagin = 1;
                        break;
                    }
                }
                if (flagin == 1) {
                    flagin = 0;
                    continue;
                }
                else {
                    flag = 0;
                    break;
                }
            }
            if(flag == 1){
                winTurns1[winTurnCount1] = j;
                winTurnCount1++;
            }
        }
    }

    public void clearGameboard(){ // очищение игровой доски после окончания игры
        PlayerId = "";
        Turn = "";
        gameNumber = 1;
        targetNumber = -1;
        winTurnCount = 0;
        winTurnCount1 = 0;
    }


    public void reply() throws IOException {
        if(gameNumber >= targetNumber){
            bot.sendAnswer("Поражение", PlayerId);
            PrintWriter pw = null; //logs
            try {
                pw = new PrintWriter(new FileWriter("logs.txt", true));
                pw.println("Победа\n");
                pw.close();
                pw = new PrintWriter(new FileWriter("stat.txt", true));
                pw.println("Победа "+PlayerId);
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //logs
            clearGameboard();
        }
        else {
            bot.sendAnswer("Текущее число: " + gameNumber, PlayerId);
            bot.sendAnswer("Ход противника", PlayerId);
            int count = d(gameNumber); // колво делителей
            int[] dlist = new int[count];//
            int counter = 0;
            for (int i = 1; i < gameNumber; i++) { //фигня делает массив делителей
                if (gameNumber % i == 0) {
                    dlist[counter] = i;
                    counter++;
                }
            }
            int x, controlNumber = targetNumber * 2 / 3 - 1; // число которое бот прибавит, мин. победное число
            /////////////////////алгоритм
            x = dlist[0];
            int flag = 0;
            for(int i = 0; i < count; i++){
                for (int k = 0; k < winTurnCount; k++) { //исключение ходов в проигрышные позиции
                    if(gameNumber + dlist[i] == winTurns[k] || gameNumber + dlist[i] >= targetNumber){
                        break;
                    }
                    else{
                        x = dlist[i];
                        flag = 1;
                        break;
                    }
                }
                if(flag == 1){
                    break;
                }
            }
            if(controlNumber > gameNumber){ //если до числа половины мы спамим по максимуму
                for (int i = 0; i < count; i++) {
                    if (gameNumber + dlist[i] < controlNumber && dlist[i]%2 == 1) {
                        x = dlist[i];
                    }
                }
            }
            flag = 0;
            for (int i = 0; i < count; i++) { //если можем сделать чтобы игрок ходил только в проигрышные - делаем
                for (int k = 0; k < winTurnCount1; k++) {
                    if (gameNumber + dlist[i] == winTurns1[k]) {
                        flag = 1;
                        x = dlist[i];
                        break;
                    }
                }
                if (flag == 1) {
                    flag = 0;
                    break;
                }
            }
            for(int i = 0; i < count; i++){ //если можно сделать победный ход, то сделать его
                if(gameNumber+dlist[i] == targetNumber - 1){
                    x = dlist[i];
                }
            }
            /////////////////////алгоритм
            PrintWriter pw = new PrintWriter(new FileWriter("logs.txt", true));
            pw.println("Target:"+targetNumber+" Number:"+gameNumber+" Turn:+"+x);
            pw.close();
            gameNumber += x;
            bot.sendAnswer("Текущее число: " + gameNumber, PlayerId);
            if(gameNumber >= targetNumber){
                bot.sendAnswer("Победа!", PlayerId);
                try {
                    pw = new PrintWriter(new FileWriter("logs.txt", true));
                    pw.println("Поражение\n");
                    pw.close();
                    pw = new PrintWriter(new FileWriter("stat.txt", true));
                    pw.println("Поражение "+PlayerId);
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //logs
                clearGameboard();
            }
            if(gameNumber <= targetNumber)bot.sendAnswer("Ваш ход", PlayerId);
        }
    }
}