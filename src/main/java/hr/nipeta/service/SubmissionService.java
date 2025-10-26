package hr.nipeta.service;

import hr.nipeta.jpa.Submission;
import hr.nipeta.model.Option;
import hr.nipeta.model.Question;
import hr.nipeta.model.QuestionType;
import hr.nipeta.model.Questionnaire;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.util.*;

@ApplicationScoped
@Slf4j
public class SubmissionService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(String questionnaireId, String submittedBy, String answersJson) {
        Submission s = new Submission(questionnaireId, submittedBy, answersJson);
        em.persist(s);
    }

    public boolean hasSubmitted(String questionnaireId, String submittedBy) {
        Long count = em.createQuery(
                        "SELECT COUNT(s) FROM Submission s WHERE s.questionnaireId = :qid AND s.submittedBy = :user", Long.class)
                .setParameter("qid", questionnaireId)
                .setParameter("user", submittedBy)
                .getSingleResult();
        return count > 0;
    }

    public List<Submission> findAll() {
        return em.createQuery("SELECT s FROM Submission s ORDER BY s.submittedAt DESC", Submission.class)
                .getResultList();
    }

    public List<Submission> findByQuestionnaire(String questionnaireId) {
        return em.createQuery(
                        "SELECT s FROM Submission s WHERE s.questionnaireId = :qid ORDER BY s.submittedAt DESC",
                        Submission.class)
                .setParameter("qid", questionnaireId)
                .getResultList();
    }


}