package hr.nipeta.jpa;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SUBMISSIONS", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"questionnaireId", "submittedBy"})
})
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUBMISSIONS_SEQ")
    @SequenceGenerator(name = "SUBMISSIONS_SEQ", sequenceName = "SUBMISSIONS_SEQ", allocationSize = 1)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private String questionnaireId;

    @ToString.Include
    private String submittedBy;

    @Lob
    @ToString.Include
    private String answersJson; // serialized answers map

    @ToString.Include
    private LocalDateTime submittedAt;

    public Submission(String questionnaireId, String submittedBy, String answersJson) {
        this.questionnaireId = questionnaireId;
        this.submittedBy = submittedBy;
        this.answersJson = answersJson;
        this.submittedAt = LocalDateTime.now();
    }

}