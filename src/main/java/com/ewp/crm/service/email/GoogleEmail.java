package com.ewp.crm.service.email;

import com.ewp.crm.exceptions.client.ClientException;
import com.ewp.crm.models.Client;
import com.ewp.crm.models.Status;
import com.ewp.crm.service.interfaces.ClientService;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.integration.mail.SearchTermStrategy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.Properties;

@Configuration
@EnableIntegration
public class GoogleEmail {

    @Value("${google.mail.login}")
    private String login;

    @Value("${google.mail.password}")
    private String password;

    @Value("${mail.from}")
    private String mailFrom;

    @Value("${mail.imap.socketFactory.class}")
    private String socketFactoryClass;

    @Value("${mail.imap.socketFactory.fallback}")
    private String socketFaktoryFallback;

    @Value("${mail.store.protocol}")
    private String protocol;

    @Value("${mail.store.protocol}")
    private String debug;

    @Value("${mail.imap.server}")
    private String imapServer;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private ClientService clientService;

    private Properties javaMailProperties() {
        Properties javaMailProperties = new Properties();

        javaMailProperties.setProperty("mail.imap.socketFactory.class", socketFactoryClass);
        javaMailProperties.setProperty("mail.imap.socketFactory.fallback", socketFaktoryFallback);
        javaMailProperties.setProperty("mail.store.protocol", protocol);
        javaMailProperties.setProperty("mail.debug", debug);

        return javaMailProperties;
    }

    @Bean
    public ImapIdleChannelAdapter mailAdapter() {
        ImapMailReceiver mailReceiver = new ImapMailReceiver("imaps://" + login + ":" + password + "@" + imapServer);

        mailReceiver.setJavaMailProperties(javaMailProperties());
        mailReceiver.setShouldDeleteMessages(false);
        mailReceiver.setShouldMarkMessagesAsRead(true);
        mailReceiver.setCancelIdleInterval(3600);
        mailReceiver.setBeanFactory(beanFactory);
        mailReceiver.setSearchTermStrategy(this::fromAndNotSeenTerm);
        mailReceiver.afterPropertiesSet();

        ImapIdleChannelAdapter imapIdleChannelAdapter = new ImapIdleChannelAdapter(mailReceiver);
        imapIdleChannelAdapter.setAutoStartup(true);
        imapIdleChannelAdapter.setReconnectDelay(300000);
        imapIdleChannelAdapter.setShouldReconnectAutomatically(true);
        imapIdleChannelAdapter.setOutputChannel(directChannel());
        imapIdleChannelAdapter.afterPropertiesSet();

        return imapIdleChannelAdapter;
    }

    @Bean
    public DirectChannel directChannel() {
        DirectChannel directChannel = new DirectChannel();
        directChannel.subscribe(new MessageHandler() {
            public void handleMessage(Message<?> message) throws MessagingException {
                MimeMessageParser parser = new MimeMessageParser((MimeMessage) message.getPayload());
                try {
                    parser.parse();
                    clientService.addClient(parseMail(parser.getPlainContent()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return directChannel;
    }

    private SearchTerm fromAndNotSeenTerm(Flags supportedFlags, Folder folder) {
        try {
            FromTerm fromTerm = new FromTerm(new InternetAddress(mailFrom));
            return new AndTerm(fromTerm, new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }

    }

    private Client parseMail(String mail) {
        Client client = new Client();
        String[] stringsOfMail = mail.split("\n");
        for (String str : stringsOfMail) {
            if (str.contains("Имя: ")) {
                client.setName(str.replace("Имя: ", ""));
            }
            if (str.contains("Name 3: ")) {
                client.setName(str.replace("Name 3: ", ""));
            }
            if (str.contains("Email 2:")) {
                client.setEmail(str.substring(10));
            }
            if (str.contains("Телефон: ")) {
                client.setPhoneNumber(str.replace("Телефон: ", ""));
            }
            if (str.contains("phone: ")) {
                client.setPhoneNumber(str.replace("phone: ", ""));
            }
        }
        client.setStatus(new Status("From email"));
        return client;
    }
}
