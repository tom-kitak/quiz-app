package commons;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompareQuestion.class, name = "CompareQuestion"),
        @JsonSubTypes.Type(value = WattageQuestion.class, name = "WattageQuestion"),
        @JsonSubTypes.Type(value = OpenQuestion.class, name = "OpenQuestion")
})




public abstract class Question {

    private final String[] answerTitles;
    private final long[] answerWattages;


    /**
     * The general constructor for questions.
     * @param answerTitles The answers for this question, index 0 is the correct one.
     * @param answerWattages The wattage's for this question, index 0 is the correct one.
     */
    public Question(String[] answerTitles, long[] answerWattages) {
        if (answerTitles != null && answerWattages != null && answerTitles.length == 4 && answerWattages.length == 4) {
            this.answerTitles = Arrays.copyOf(answerTitles, answerTitles.length);
            this.answerWattages = Arrays.copyOf(answerWattages, answerWattages.length);
        } else {
            throw new IllegalArgumentException("The provided arrays were null or not length 4.");
        }
    }

    /**
     * Used for transferring the questions.
     */
    public  Question() {
        this.answerTitles = null;
        this.answerWattages = null;
    }

    /**
     * Getter for the answer title's.
     * @return this.answerTitles.
     */
    public String[] getAnswerTitles() {
        return Arrays.copyOf(answerTitles, answerTitles.length);
    }

    /**
     * Getter for the answer wattage's.
     * @return this.answerWattages.
     */
    public long[] getAnswerWattages() {
        return Arrays.copyOf(answerWattages, answerWattages.length);
    }

    /**
     * Getter for the correct answer title.
     * @return index 0 of this.answerTitles.
     */
    @JsonIgnore
    public String getCorrectAnswer() {
        return answerTitles[0];
    }

    /**
     * Getter for the correct answer wattage.
     * @return index 0 of this.answerWattages.
     */
    @JsonIgnore
    public long getCorrectWattage() {
        return answerWattages[0];
    }

    /**
     * Equals method for this object.
     * @param o Object to be compared against.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Question)) return false;

        Question question = (Question) o;

        return new EqualsBuilder().append(answerTitles, question.answerTitles)
                .append(answerWattages, question.answerWattages).isEquals();
    }

    /**
     * Hashcode generator for questions.
     * @return Hashcode of this question.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(answerTitles).append(answerWattages).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("answerTitles", answerTitles)
                .append("answerWattages", answerWattages)
                .toString();
    }

    /**
     * @return a String that represents the description of
     * the question that will be shown to the client
     */
    public abstract String getQuestionDescription();




}
