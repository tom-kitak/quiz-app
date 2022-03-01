package commons;



import java.util.ArrayList;
import java.util.Objects;



public class Question {

    private final String question;
    private final int rightAnswer;
    private ArrayList<Integer> wrongAnswers;

    /**Generates new Question Object.
     * @param question the String that represents the question itself
     * @param rightAnswer the integer that is the right answer to the question in wh
     */
    public Question(String question, int rightAnswer) {
        this.question = question;
        this.rightAnswer = rightAnswer;
        wrongAnswers = assignWrongAnswers(rightAnswer);
    }


    /**Generates wrong answers to the question.
     * @param rightAnswer the right answer to the question
     * @return ArrayList with 3 wrong answers;
     */
    public ArrayList<Integer> assignWrongAnswers(int rightAnswer) {
        ArrayList<Integer> randomAnswers = new ArrayList<>(3);

        if (rightAnswer <= 3){
            for (int i =1; i <= 4; i++){
                if(i!=rightAnswer){
                    randomAnswers.add(i);
                }
            }
            return randomAnswers;
        } else {
            int min = (int) Math.round(0.49*rightAnswer);
            while(randomAnswers.size()<3){
                double factor = Math.random();
                int number = (int) ((factor*rightAnswer) + min);
                if(!randomAnswers.contains(number)&&number!=rightAnswer){
                    randomAnswers.add(number);
                }
            }
            return  randomAnswers;

        }

    }

    /**
     * @return the question attribute
     */
    public String getQuestion() {
        return this.question;
    }

    /**
     * @return the rightAnswer attribute of this object
     */
    public int getRightAnswer() {
        return this.rightAnswer;
    }

    /**
     * @return the list of wrong answers of this Object
     */
    public ArrayList<Integer> getWrongAnswers() {
        return this.wrongAnswers;
    }

    /**checks if two objects are equal.
     * @param o the object we want to compare this Question with
     * @return true iff o and this Question have the same right answer and
     * the same question
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question1 = (Question) o;
        return rightAnswer == question1.rightAnswer && question.equals(question1.question);
    }

    /**
     * @return hashcode for this Question
     */
    @Override
    public int hashCode() {
        return Objects.hash(question, rightAnswer);
    }

    /**
     * @return a String representation of the object in Multi_Line_Style
     */
    @Override
    public String toString() {
        String result = "Question: \nquestion=" + this.question +"\nrightAnswer=" + this.rightAnswer +
                "\nwrongAnswers=[" + wrongAnswers.get(0) +", " + wrongAnswers.get(1) + ", "
                + wrongAnswers.get(2) + "]";
        return result;
    }
}
