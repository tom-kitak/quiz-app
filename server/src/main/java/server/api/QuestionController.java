package server.api;

import commons.Question;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import commons.Activity;
import server.database.ActivityRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static server.util.QuestionConversion.convertActivity;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final Random random;
    private final ActivityRepository repo;

    public QuestionController(Random random, ActivityRepository repo) {
        this.random = random;
        this.repo = repo;

        if(repo.count() < 4) {
            repo.save(new Activity("Using a laptop for 10 hours", 10));
            repo.save(new Activity("Using a TV for 7 hours", 5));
            repo.save(new Activity("Using a phone for 12 hours", 15));
            repo.save(new Activity("Using a toaster for 20 picoseconds", 20));
        }
    }

    @GetMapping(path = {"", "/"})
    public ResponseEntity<Question> getQuestion() {
        // Can't create a question if there aren't enough activities
        if (repo.count() < 4)
            return ResponseEntity.internalServerError().build();
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
        if (question == null)
            return ResponseEntity.internalServerError().build();
        return ResponseEntity.ok(question);
    }
}