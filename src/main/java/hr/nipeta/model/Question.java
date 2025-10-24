package hr.nipeta.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class Question implements Serializable {
    private String id;
    private QuestionType type;
    private String label;
    private String placeholder;
    private String help;
    private boolean required;
    private Integer maxSelected;   // for multi
    private Integer scale;         // for rating
    private List<Option> options;  // for single/multi

    // simple validators
    private Integer minLength;
    private Integer maxLength;
    private String pattern;

}