package com.ewp.crm.service.slack;

import com.ewp.crm.models.SocialProfile;
import com.ewp.crm.models.SocialProfileType;
import com.ewp.crm.models.Student;
import com.ewp.crm.service.interfaces.ClientService;
import com.ewp.crm.service.interfaces.SlackService;
import com.ewp.crm.service.interfaces.SocialProfileTypeService;
import com.ewp.crm.service.interfaces.StudentService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@PropertySources(value = {
        @PropertySource("classpath:application.properties"),
        @PropertySource("file:./slack.properties")
})
public class SlackServiceImpl implements SlackService {

    private static Logger logger = LoggerFactory.getLogger(SlackServiceImpl.class);
    private final StudentService studentService;
    private final SocialProfileTypeService socialProfileTypeService;
    private final ClientService clientService;
    private final String inviteToken;

    @Autowired
    public SlackServiceImpl(Environment environment, StudentService studentService, SocialProfileTypeService socialProfileTypeService, ClientService clientService) {
        this.inviteToken = environment.getRequiredProperty("slack.legacyToken");
        if (inviteToken.isEmpty()) {
            logger.warn("Can't get slack.legacyToken get it from https://api.slack.com/custom-integrations/legacy-tokens");
        }
        this.studentService = studentService;
        this.socialProfileTypeService = socialProfileTypeService;
        this.clientService = clientService;
    }

    @Override
    public boolean tryLinkSlackAccountToStudent(long studentId) {
        double emailWeight = 1.0d;
        double nameWeight = 0.25d;
        double lastNameWeight = 0.25d;
        Map<String, Double> result = new HashMap<>();
        Optional<String> allWorkspaceUsersData = receiveAllClientsFromWorkspace();
        Student student = studentService.get(studentId);
        if (allWorkspaceUsersData.isPresent()) {
            Map<String, String[]> data = parseSlackUsersFromJson(allWorkspaceUsersData.get());
            for (Map.Entry<String, String[]> entry :data.entrySet()) {
                double weight = 0d;
                String id = entry.getKey();
                String name = entry.getValue()[0];
                String email = entry.getValue()[1];
                if (email != null && email.equals(student.getClient().getEmail())) {
                    weight += emailWeight;
                }
                if (name != null) {
                    if (name.contains(student.getClient().getName())) {
                        weight += nameWeight;
                    }
                    if (name.contains(student.getClient().getLastName())) {
                        weight += lastNameWeight;
                    }
                }
                if (weight >= nameWeight + lastNameWeight) {
                    result.put(id, weight);
                }
            }
            if (!result.isEmpty()) {
                Optional<Map.Entry<String, Double>> entry = result.entrySet().stream().max(Map.Entry.comparingByValue());
                if (entry.isPresent()) {
                    Optional<SocialProfileType> slackSocialProfile = socialProfileTypeService.getByTypeName("slack");
                    if (slackSocialProfile.isPresent()) {
                        student.getClient().addSocialProfile(new SocialProfile(entry.get().getKey(), slackSocialProfile.get()));
                        clientService.updateClient(student.getClient());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Optional<String> getEmailListFromJson(String json) {
        Map<String, String[]> data = parseSlackUsersFromJson(json);
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String[]> entry :data.entrySet()) {
            String email = entry.getValue()[1];
            if (email != null && !email.isEmpty()){
                result.append(email).append("\n");
            }
        }
        return result.toString().isEmpty() ? Optional.empty() : Optional.of(result.toString());
    }

    @Override
    public Optional<String> receiveAllClientsFromWorkspace() {
        String url = "https://slack.com/api/users.list?token=" + inviteToken;
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(new HttpGet(url))) {
            HttpEntity entity = response.getEntity();
            return Optional.ofNullable(EntityUtils.toString(entity));
        } catch (IOException e) {
            logger.error("Can't get data from Slack server", e);
        }
        return Optional.empty();
    }

    /**
     * Get slack users data and put it to map with user 'id' as a key and array [email, name] as value
     */
    private Map<String, String[]> parseSlackUsersFromJson(String json) {
        Map<String, String[]> result = new HashMap<>();
        try {
            JSONObject jsonObj = new JSONObject(json);
            JSONArray jsonData = jsonObj.getJSONArray("members");
            for (int i = 0; i < jsonData.length(); i++) {
                JSONObject current = jsonData.getJSONObject(i);
                JSONObject userProfile = current.optJSONObject("profile");
                if (userProfile == null) {
                    continue;
                }
                String id = current.optString("id");
                String mail = userProfile.optString("email");
                String name = userProfile.optString("real_name");
                result.put(id, new String[]{name, mail});
            }
        } catch (JSONException e) {
            logger.error("Can't parse users from slack", e);
        }
        return result;
    }

}
