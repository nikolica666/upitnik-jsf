package hr.nipeta.service;

import hr.nipeta.model.Option;
import hr.nipeta.model.Question;
import hr.nipeta.model.QuestionType;
import hr.nipeta.model.Questionnaire;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.InputStream;
import java.util.*;

@ApplicationScoped
@Slf4j
public class QuestionnaireService {

    public static final List<String> QUESTIONNAIRES = Arrays.asList("llm-current", "llm-future", "simple", "simple-expired");

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

    public Questionnaire byId(String id) {
        return getAll().get(id);
    }

    private Questionnaire loadJson(String path) {

        try (InputStream is = getClass().getResourceAsStream(path)) {

            if (is == null) {
                return null;
            }

            JsonObject root = Json.createReader(is).readObject();
            Questionnaire qn = new Questionnaire();
            qn.setId(root.getString("id"));
            qn.setTitle(root.getString("title", qn.getId()));
            qn.setDescription(root.getString("description", ""));
            qn.setVersion(root.getInt("version", 1));

            List<Question> questions = new ArrayList<>();
            for (JsonObject jq : root.getJsonArray("questions").getValuesAs(JsonObject.class)) {
                Question q = new Question();
                q.setId(jq.getString("id"));
                q.setType(QuestionType.valueOf(jq.getString("type")));
                q.setLabel(jq.getString("label", q.getId()));
                q.setPlaceholder(jq.getString("placeholder", null));
                q.setHelp(jq.getString("help", null));
                q.setRequired(jq.getBoolean("required", true)); // default true
                if (jq.containsKey("maxSelected")) q.setMaxSelected(jq.getInt("maxSelected"));
                if (jq.containsKey("scale")) q.setScale(jq.getInt("scale"));
                if (jq.containsKey("validators")) {
                    JsonObject v = jq.getJsonObject("validators");
                    if (v.containsKey("minLength")) q.setMinLength(v.getInt("minLength"));
                    if (v.containsKey("maxLength")) q.setMaxLength(v.getInt("maxLength"));
                    if (v.containsKey("pattern")) q.setPattern(v.isNull("pattern") ? null : v.getString("pattern"));
                }
                if (jq.containsKey("options")) {
                    List<Option> opts = new ArrayList<>();
                    for (JsonObject jo : jq.getJsonArray("options").getValuesAs(JsonObject.class)) {
                        Option o = new Option();
                        o.setValue(jo.getString("value"));
                        o.setLabel(jo.getString("label", o.getValue()));
                        opts.add(o);
                    }
                    q.setOptions(opts);
                }
                questions.add(q);
            }
            qn.setQuestions(questions);
            return qn;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}