package hr.nipeta.web;

import hr.nipeta.model.Question;
import hr.nipeta.model.QuestionType;
import hr.nipeta.model.Questionnaire;
import hr.nipeta.service.QuestionnaireService;
import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.SessionScoped; // keep answers across pages; switch to ViewScoped if you prefer per-view
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

@Named
@SessionScoped
@Getter
public class QuestionnaireBean implements Serializable {

    @Inject
    private QuestionnaireService service;

    private String questionnaireId;
    private Questionnaire questionnaire;

    @Setter
    private Map<String, Object> answers = new LinkedHashMap<>();
    private int index = 0; // current question index; or render all at once

    public void load(String id) {
        if (questionnaire == null || !Objects.equals(id, questionnaireId)) {
            this.questionnaireId = id;
            this.questionnaire = service.byId(id);
            this.answers = new LinkedHashMap<>();
            this.index = 0;
        }
    }

    public List<Questionnaire> listAll() {
        return new ArrayList<>(service.getAll().values());
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
        // TODO: persist to DB or send to API
        System.out.println("Submitted answers for " + questionnaireId + ": " + answers);
        String done = questionnaireId;
        questionnaire = null; answers.clear(); index = 0; questionnaireId = null;
        return "index?faces-redirect=true&done=" + done;
    }

    public String onFlowProcess(org.primefaces.event.FlowEvent event) {
        if (!validateCurrent()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Please complete this question."));
            return event.getOldStep();
        }
        index = questionnaire.getQuestions().indexOf(findQuestionByStep(event.getNewStep()));
        return event.getNewStep();
    }

    private Question findQuestionByStep(String stepId) {
        int idx = Integer.parseInt(stepId.replace("step", ""));
        return questionnaire.getQuestions().get(idx);
    }

    // navigation
    public void next() { if (index < questionnaire.getQuestions().size()-1 && validateCurrent()) index++; }
    public void prev() { if (index > 0) index--; }

    public boolean isLast() { return questionnaire != null && index >= questionnaire.getQuestions().size() - 1; }

}