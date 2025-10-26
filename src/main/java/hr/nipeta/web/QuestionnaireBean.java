package hr.nipeta.web;

import hr.nipeta.model.Question;
import hr.nipeta.model.QuestionType;
import hr.nipeta.model.Questionnaire;
import hr.nipeta.service.QuestionnaireService;
import hr.nipeta.service.SubmissionService;
import hr.nipeta.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static hr.nipeta.service.QuestionnaireService.QUESTIONNAIRES;


@Named
@SessionScoped
@Getter
@Slf4j
public class QuestionnaireBean implements Serializable {

    @Inject
    private QuestionnaireService questionnaireService;

    @Inject
    private SubmissionService submissionService;

    @Setter
    private String questionnaireId;
    private Questionnaire questionnaire;

    @Setter
    private Map<String, Object> answers = new LinkedHashMap<>();
    private int index = 0; // current question index; or render all at once

    public void load() throws IOException {

        if (questionnaireId == null || questionnaireId.isEmpty()) {
            Messages.addFlashGlobalError("Preusmjereni ste na početni ekran jer upitnik nije naveden");
            Faces.redirect("index.xhtml");
            resetQuestionnaire();
            return;
        }

        if (!QUESTIONNAIRES.contains(questionnaireId)) {
            Messages.addFlashGlobalError("Preusmjereni ste na početni ekran jer ne postoji upitnik za ID ''{0}''", questionnaireId);
            Faces.redirect("index.xhtml");
            resetQuestionnaire();
            return;
        }

        if (questionnaire == null || !Objects.equals(questionnaire.getId(), questionnaireId)) {
            questionnaire = questionnaireService.byId(questionnaireId);
            if (questionnaire == null || !questionnaire.isCurrentlyValid()) {
                Messages.addGlobalError("Ovaj upitnik je neaktivan ili ne postoji");
                questionnaire = null;
                return;
            }
            answers = new LinkedHashMap<>();
            for (Question q : questionnaire.getQuestions()) {
                if (Objects.requireNonNull(q.getType()) == QuestionType.multi) {
                    answers.put(q.getId(), new ArrayList<>());
                } else {
                    answers.put(q.getId(), null);
                }
            }
            index = 0;
        }
    }

    public List<Questionnaire> listAll() {
        return new ArrayList<>(questionnaireService.getAll().values());
    }

    public boolean validateCurrent() {
        if (questionnaire == null) return false;
        Question q = questionnaire.getQuestions().get(index);
        Object val = answers.get(q.getId());
        // required
        if (q.isRequired()) {
            if (val == null) return false;
            if (val instanceof String && ((String)val).trim().isEmpty()) return false;
            if (val instanceof Collection && ((Collection<?>)val).isEmpty()) return false;
        }
        // simple length checks
        if (val instanceof String) {
            String s = (String) val;
            if (q.getMinLength()!=null && s.length() < q.getMinLength()) return false;
            if (q.getMaxLength()!=null && s.length() > q.getMaxLength()) return false;
            if (q.getPattern()!=null && !s.matches(q.getPattern())) return false;
        }
        // multi max
        if (q.getType()== QuestionType.multi && q.getMaxSelected()!=null) {
            Collection<?> c = (Collection<?>) val;
            if (c != null && c.size() > q.getMaxSelected()) return false;
        }
        return true;
    }

    public String submit() {

        String username = Faces.getRemoteUser();
        if (username == null) {
            log.warn("Postavljam 'anonymus' korisnika jer je remote user null");
            username = "anonymus";
        }

        if (submissionService.hasSubmitted(questionnaire.getId(), username)) {
            Messages.addGlobalWarn("Upitnik već predan");
            return null;
        }

        String json = JsonUtils.serializeToJson(answers);
        submissionService.save(questionnaire.getId(), username, json);

        String done = questionnaireId;

        resetQuestionnaire();

        return "index?faces-redirect=true&done=" + done;

    }

    private void resetQuestionnaire() {
        questionnaire = null;
        answers.clear();
        index = 0;
        questionnaireId = null;
    }

    private Question findQuestionByStep(String stepId) {
        int idx = Integer.parseInt(stepId.replace("question", ""));
        return questionnaire.getQuestions().get(idx);
    }

    // navigation
    public void next() {
        if (validateCurrent() && index < questionnaire.getQuestions().size() - 1) {
            index++;
        }
    }

    public void prev() {
        if (index > 0) {
            index--;
        }
    }

}