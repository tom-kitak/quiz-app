package server.api;

import commons.Emoji;
import commons.MultiGame;
import commons.Player;
import commons.Activity;
import commons.Question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import server.database.ActivityRepository;
//CHECKSTYLE:OFF
import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

import static server.util.QuestionConversion.convertActivity;

@RestController
public class MultiPlayerController {
    private ArrayList<MultiGame> games;
    private MultiGame currentLobbyGame;
    private final Random random;
    private final ActivityRepository repo;
    private int id;
    private ArrayList<Integer> allPlayersResponded;
    private ArrayList<Integer> currentPlayerCount;
    private ArrayList<TimerTask> lobbyTimers;
    private Timer timer;

    @Autowired
    private SimpMessagingTemplate template;

    /**
     * Creates a new MultiplayerController object.
     * @param random a new Random object
     * @param repo the activity repository
     */
    public MultiPlayerController(Random random, ActivityRepository repo) {
        this.random = random;
        this.id = 0;
        this.repo = repo;
        this.games = new ArrayList<>();
        this.currentLobbyGame = new MultiGame(null);
        currentLobbyGame.setId(id);
        this.allPlayersResponded = new ArrayList<>();
        allPlayersResponded.add(0);
        this.currentPlayerCount = new ArrayList<>();
        this.lobbyTimers = new ArrayList<>();
        timer = new Timer();
    }

    /**
     * Called when the player connects or disconnect from the lobby.
     * @param player the player send by the client
     * @return the game object that acts as the lobby
     */
    @MessageMapping("/multi")
    @SendTo("/topic/multi")
    public MultiGame connect(Player player){
        System.out.println("-----------------");
        System.out.println(player);
        ArrayList<Player> tempPlayers = currentLobbyGame.getPlayers();
        if(tempPlayers.contains(player)){
            tempPlayers.remove(player);
            System.out.println("Disconnected\n");
        } else {
            tempPlayers.add(player);
            System.out.println("Connected\n");
        }
        currentLobbyGame.setPlayers(tempPlayers);

        return currentLobbyGame;
    }

    /**
     * Starts the game and creates a new lobby.
     * @return the game that started and sends it to that lobby
     */
    @MessageMapping("/start")
    @SendTo("/topic/started")
    public MultiGame startGame() {
        Question question = getQuestion();
        MultiGame started = currentLobbyGame;
        started.setCurrentQuestion(question);
        this.id++;
        currentLobbyGame = new MultiGame(null);
        currentLobbyGame.setId(id);
        // We put 0 responses, in a spot associated with game id, since they start at 0
        // We increment it for every response, and reset it when needed.
        allPlayersResponded.add(0);
        games.add(started);
        currentPlayerCount.add(started.getPlayers().size());
        lobbyTimers.add(null);
        controlPlayerResponse(started);
        return started;
    }

    /**
     * Gets a question by generating 4 random activities from the server/
     * @return a random Question
     */
    public Question getQuestion() {
        // Can't create a question if there aren't enough activities
        if (repo.count() < 4)
            return null;
        Activity[] activities = new Activity[4];
        // This is a workaround for the id generation that isn't consistent
        // This works now but will be slow in the future, so we need to research better id assignment.
        List<Activity> currentRepo = repo.findAll();
        // Collects the 4 activities needed for a question
        for (int i = 0; i < 4; i++) {

            // Picks a random activity
            var idx = random.nextInt(currentRepo.size());
            Activity a = currentRepo.get(idx);
            // Makes a list of current activities and checks for duplicates
            // Old implementation changed because of a java API 1.6 error.
            List<Activity> list = new ArrayList<>();
            Collections.addAll(list, activities);
            // Adds the activity or reruns the iteration.
            if (!list.contains(a)) {
                activities[i] = a;
            } else {
                i--;
            }
        }
        // Returns the result.
        Question question = convertActivity(activities);
        return question;
    }

    private void controlPlayerResponse(MultiGame game) {
        TimerTask roundTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Lobby count lowered");
                currentPlayerCount.set(game.getId(), allPlayersResponded.get(game.getId()));
                gameplayQuestionSender(String.valueOf(game.getId()), game);
            }
        };
        timer.schedule(roundTask, 20 * 1000);
        lobbyTimers.set(game.getId(), roundTask);
    }

    /**
     * Will generate a new Question and send it to the players of the certain lobby if everyone answered.
     * @param gameId the integer that identifies the game in which the sender is engaged
     * @param gameFromPlayer the MultiGame that represents the lobby of this player
     * @return
     */
    @MessageMapping("/multi/gameplay/{gameId}")
    @SendTo("/topic/multi/gameplay/{gameId}")
    public MultiGame gameplayQuestionSender(@DestinationVariable String gameId, MultiGame gameFromPlayer) {
        MultiGame game = null;
        for (MultiGame g : games){
            if (g.getId() == Integer.valueOf(gameId)){
                game = g;
                break;
            }
        }
        for(int i = 0; i < gameFromPlayer.getPlayers().size(); i++) {
            if(game.getPlayers().get(i) != gameFromPlayer.getPlayers().get(i)) {
                game.getPlayers().set(i, gameFromPlayer.getPlayers().get(i));
            }
        }
        // Controls the responses, that players made.
        allPlayersResponded.set(game.getId(), allPlayersResponded.get(game.getId()) + 1);

        if (currentPlayerCount.get(game.getId()) <= allPlayersResponded.get(game.getId())){
            lobbyTimers.get(game.getId()).cancel();
            game.setCurrentQuestion(getQuestion());
            game.setQuestionNumber(game.getQuestionNumber() + 1);
            System.out.println(game.getCurrentQuestion());
            allPlayersResponded.set(game.getId(), 0);
            System.out.println("Response for game " + gameId + " send!");
            if(currentPlayerCount.get(game.getId()) > 0) {
                controlPlayerResponse(game);
            }
            this.template.convertAndSend("/topic/multi/gameplay/" + gameId, game);
        }
        return null;
    }

    /**
     * Generates a new Question and sends the Multigame object.
     * @param gameId the integer that identifies the game in which the sender is engaged
     * @param game the MultiGame that represents the lobby of this player
     * @return
     */
    @SendTo("/topic/multi/gameplay/{gameId}")
    public MultiGame sendQuestion(@DestinationVariable String gameId,MultiGame game) {
        System.out.println(gameId);
        game.setCurrentQuestion(getQuestion());
        return game;
    }

    /**
     * Will delete the Player from the MultiGame object while they are answering questions.
     * @param gameId the id of the MultiGame in which the Player is engaged
     * @param player the player that disconnects
     */
    @MessageMapping("/multi/leaveInGame/{gameId}")
    public void disconnect(@DestinationVariable String gameId, Player player) {
        MultiGame game= null;
        for (MultiGame g : games) {
            if (g.getId() == Integer.valueOf(gameId)) {
                game = g;
                break;
            }
        }
        if(game != null){
            ArrayList<Player> players = game.getPlayers();
            players.remove(player);
            game.setPlayers(players);
        }
    }


    /**
     * Gets the current lobby that is waiting in the waiting room.
     * @return the MultiGame object that represents the current lobby
     */
    @GetMapping("topic/lobby")
    public ResponseEntity<MultiGame> getLobby(){
        return ResponseEntity.ok(currentLobbyGame);
    }


    /**
     * Sends a game to the lobby in where the shorten-time-joker is used.
     * @param gameId the gameId that identifies their MultiGame lobby
     * @param game the game representing the lobby in which the joker was called
     * @return the game object representing that lobby
     */
    @MessageMapping("/multi/jokers/{gameId}")
    @SendTo("/topic/multi/jokers/{gameId}")
    public MultiGame shortenTime(@DestinationVariable String gameId, MultiGame game) {
        return game;
    }


    /**
     * Will send the type of the emoji to the right lobby.
     * @param type the String representing which type of emoji is sent
     * @param emoji the actual emoji that was sent
     * @return the emoji
     */
    @MessageMapping("/multi/emoji/{type}")
    @SendTo("/topic/multi/emoji/{type}")
    public Emoji emojiHandler(@DestinationVariable String type, Emoji emoji){
        return emoji;
    }

}
