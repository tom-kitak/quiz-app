package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiGameTest {

    private MultiGame game;
    private ArrayList<Player> players;
    private Player player;
    private Question question;

    @BeforeEach
    void setUp(){
        player = new Player("j");
        players = new ArrayList<>();
        players.add(player);
        long[] wattages = new long[4];
        wattages[0] = 1;
        wattages[1] = 2;
        wattages[2] = 3;
        wattages[3] = 4;
        String[] descriptions = new String[4];
        descriptions[0] = "a";
        descriptions[1] = "b";
        descriptions[2]= "c";
        descriptions[3] = "d";
        question = new CompareQuestion(descriptions, wattages);
        this.game = new MultiGame(question);
    }

    @Test
    void testConstructor(){
        assertNotNull(game);
    }

    @Test
    void addPlayer() {
        game.addPlayer(player);
        assertEquals(players, game.getPlayers());
    }

    @Test
    void removePlayer() {
        game.addPlayer(player);
        game.removePlayer(player);
        ArrayList<Player> test = new ArrayList<>();
        assertEquals(test, game.getPlayers());
    }

    @Test
    void getPlayers() {
        ArrayList<Player> test = new ArrayList<>();
        assertEquals(test, game.getPlayers());
    }

    @Test
    void setPlayers() {
        game.setPlayers(players);
        assertEquals(players, game.getPlayers());
    }

    @Test
    void testEquals() {
        MultiGame game2 = new MultiGame(question);
        assertEquals(game, game2);
    }

    @Test
    void testInequality() {
        MultiGame game2 = new MultiGame(question);
        game2.addPlayer(player);
        assertNotEquals(game, game2);
    }

    @Test
    void testInequality2(){
        long[] wattages = new long[4];
        wattages[0] = 1;
        wattages[1] = 2;
        wattages[2] = 3;
        wattages[3] = 4;
        String[] descriptions = new String[4];
        descriptions[0] = "a";
        descriptions[1] = "b";
        descriptions[2]= "c";
        descriptions[3] = "d";
        Question question2 = new WattageQuestion(descriptions, wattages);
        MultiGame game2 = new MultiGame(question2);
        assertNotEquals(game2, game);
    }

    @Test
    void testHashCode() {
        MultiGame game2 = new MultiGame(question);
        assertEquals(game.hashCode(), game2.hashCode());
    }

    @Test
    void testDifferentHashCodes(){
        long[] wattages = new long[4];
        wattages[0] = 1;
        wattages[1] = 2;
        wattages[2] = 3;
        wattages[3] = 4;
        String[] descriptions = new String[4];
        descriptions[0] = "a";
        descriptions[1] = "b";
        descriptions[2]= "c";
        descriptions[3] = "e";
        Question question2 = new WattageQuestion(descriptions, wattages);
        MultiGame game2 = new MultiGame(question2);
        assertNotEquals(game2.hashCode(), game.hashCode());
    }

    @Test
    void testToString() {
        String result = game.toString();
        assertTrue(result.contains("MultiGame") && result.contains("players")
                &&result.contains("questionNumber") && result.contains("currentQuestion"));
    }
}