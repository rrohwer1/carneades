/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.fokus.carneades.simulation;

import java.util.ArrayList;
import java.util.List;
import org.fokus.carneades.api.Statement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author stb, bbr
 */
// TODO : implement some useful mapping using ontology annotations
public class QuestionHelper {
    
    private static final Logger log = LoggerFactory.getLogger(QuestionHelper.class);
       
    public static JSONObject getJSONFromQuestions(List<Question> qList, String lang) {
        // TODO : what to do with id? - ID simply numbered, e.g. 1st question's id = 1, 2nd = 2 etc.

        JSONObject jsonQuestions = new JSONObject();
        try {            
            JSONArray qArray = new JSONArray();
            for(Question q : qList) {
                qArray.put(q.toJSON(lang));
            }
            jsonQuestions.put("questions", qArray);
        } catch (JSONException e) {
            log.error("could not transform questions: " + e.getMessage(), qList);
        } finally {
            return jsonQuestions;
        }
    }

    public static List<Statement> mapAnswersAndQuestionsToStatement (List<Question> qList, List<Answer> aList) {
        List<Statement> result = new ArrayList<Statement>();
        for (Answer answer : aList) {
            int id = answer.getId();
            Question q = null;
            for (Question q1 : qList) {
                if (id == q1.getId()) q = q1;
            }
            Statement stmt = q.getStatement();
            List<String> newArgs = new ArrayList<String>();
            for (String arg : stmt.getArgs()) {
                if (arg.startsWith("?")) {
                    newArgs.add(answer.getValue());
                } else {
                    newArgs.add(arg);
                }
            }
            stmt.setArgs(newArgs);
            result.add(stmt);
        }
        return result;
    }

}