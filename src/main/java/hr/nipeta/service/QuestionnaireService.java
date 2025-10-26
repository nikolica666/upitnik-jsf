package hr.nipeta.service;

import hr.nipeta.model.Option;
import hr.nipeta.model.Question;
import hr.nipeta.model.QuestionType;
import hr.nipeta.model.Questionnaire;
import hr.nipeta.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.*;
import java.util.function.IntConsumer;

@ApplicationScoped
@Slf4j
public class QuestionnaireService {

    public static final List<String> QUESTIONNAIRES = Arrays.asList(
            "llm-current",
            "llm-future",
            "simple",
            "simple-expired");

    private Map<String, Questionnaire> cache;

    public synchronized Map<String, Questionnaire> getAll() {

        if (cache != null) {
            return cache;
        }

        cache = new LinkedHashMap<>();
        // discover bundled JSON files; for simplicity list them here or scan the folder if your container allows it
        for (String id : QUESTIONNAIRES) {
            log.debug("Searching for {}.json", id);
            Questionnaire q = loadJson("/questionnaires/" + id + ".json");
            if (q != null) {
                log.debug("Found {}.json, adding to cache", id);
                cache.put(q.getId(), q);
            }
        }

        return cache;

    }

    private Questionnaire loadJson(String path) {
        return JsonUtils.loadJson(path, this::parseQuestionnaire);
    }

    private Questionnaire parseQuestionnaire(JsonObject root) {
        Questionnaire qn = new Questionnaire();
        qn.setId(root.getString("id"));
        qn.setTitle(root.getString("title", qn.getId()));
        qn.setDescription(root.getString("description", ""));
        qn.setVersion(root.getInt("version", 1));

        List<Question> questions = new ArrayList<>();
        for (JsonObject jq : root.getJsonArray("questions").getValuesAs(JsonObject.class)) {
            questions.add(parseQuestion(jq));
        }

        qn.setQuestions(questions);
        return qn;
    }

    private Question parseQuestion(JsonObject jq) {
        Question q = new Question();
        q.setId(jq.getString("id"));
        q.setType(QuestionType.valueOf(jq.getString("type")));
        q.setLabel(jq.getString("label", q.getId()));
        q.setPlaceholder(jq.getString("placeholder", null));
        q.setHelp(jq.getString("help", null));
        q.setRequired(jq.getBoolean("required", true)); // default true

        // optional simple numeric fields
        setIfPresent(jq, "maxSelected", q::setMaxSelected);
        setIfPresent(jq, "scale", q::setScale);

        // nested validators
        if (jq.containsKey("validators")) {
            parseValidators(jq.getJsonObject("validators"), q);
        }

        // options list
        if (jq.containsKey("options")) {
            q.setOptions(parseOptions(jq.getJsonArray("options")));
        }

        return q;
    }

    private void parseValidators(JsonObject v, Question q) {
        setIfPresent(v, "minLength", q::setMinLength);
        setIfPresent(v, "maxLength", q::setMaxLength);
        if (v.containsKey("pattern")) {
            q.setPattern(v.isNull("pattern") ? null : v.getString("pattern"));
        }
    }

    private List<Option> parseOptions(JsonArray array) {
        List<Option> opts = new ArrayList<>();
        for (JsonObject jo : array.getValuesAs(JsonObject.class)) {
            Option o = new Option();
            o.setValue(jo.getString("value"));
            o.setLabel(jo.getString("label", o.getValue()));
            opts.add(o);
        }
        return opts;
    }

    private void setIfPresent(JsonObject obj, String key, IntConsumer setter) {
        if (obj.containsKey(key)) setter.accept(obj.getInt(key));
    }

    public Questionnaire byId(String id) {
        return getAll().get(id);
    }

}