package com.ewp.crm.controllers.rest;

import com.ewp.crm.models.Client;
import com.ewp.crm.service.interfaces.ClientService;
import com.ewp.crm.service.interfaces.SlackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * TODO before start.
 * Docs about slack functions installation
 * 1. Goto https://api.slack.com/apps
 * 2. Choice app for Workspace (JavaMentor or something else)
 * 3. Goto Features -> Event Subscriptions
 * 4. Turn ON "Enable Events'
 * TODO after start
 * 5. Put at "Request URL" app-IP/slack or app-URL/slack
 * 6. Wait for verify
 * 7. Add to "Add Bot User Event" event with name "member_joined_channel"
 * 8. TODO Проверить и дописать инструкцию. Продублировать.
 */

@RestController
@RequestMapping("/slack")
public class SlackRestController {

    private static final Logger logger = LoggerFactory.getLogger(SlackRestController.class);

    private final SlackService slackService;
    private final ClientService clientService;
    private final String inviteToken;

    @Autowired
    public SlackRestController(ClientService clientService, Environment environment, SlackService slackService) {
        this.inviteToken = environment.getRequiredProperty("slack.legacyToken");
        if (inviteToken.isEmpty()) {
            logger.warn("Can't get slack.legacyToken get it from https://api.slack.com/custom-integrations/legacy-tokens");
        }
        this.slackService = slackService;
        this.clientService = clientService;
    }

    @GetMapping("/find/client/{clientId}")
    public ResponseEntity<String> findClientSlackProfile(@PathVariable long clientId) {
        Optional<Client> client = clientService.getClientByID(clientId);
        if (client.isPresent() && client.get().getStudent() != null) {
            return findStudentSlackProfile(client.get().getStudent().getId());
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/find/student/{studentId}")
    public ResponseEntity<String> findStudentSlackProfile(@PathVariable long studentId) {
        if (slackService.tryLinkSlackAccountToStudent(studentId)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/send/student/{studentId}")
    public ResponseEntity sendMessageToStudent(@PathVariable long studentId, @RequestParam("text") String text) {
        if (slackService.trySendSlackMessageToStudent(studentId, text)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @PostMapping("/send/all")
    public ResponseEntity sendMessageToAllSlackUsers(@RequestParam("text") String text) {
        if (slackService.trySendMessageToAllSlackUsers(text)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/get/emails")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'USER')")
    public ResponseEntity<String> getAllEmailsFromSlack() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "text/plain;charset=UTF-8");
        return new ResponseEntity<>(slackService.getAllEmailsFromSlack().orElse("Error"), headers, HttpStatus.OK);
    }
}
