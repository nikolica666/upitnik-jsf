package hr.nipeta.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Questionnaire implements Serializable {

    private String id;
    private String title;
    private String description;
    private int version;
    private List<Question> questions;

    private LocalDate validFrom;
    private LocalDate validTo;

    public boolean isCurrentlyValid() {
        LocalDate today = LocalDate.now();
        boolean afterStart = validFrom == null || !today.isBefore(validFrom);
        boolean beforeEnd = validTo == null || !today.isAfter(validTo);
        return afterStart && beforeEnd;
    }

}