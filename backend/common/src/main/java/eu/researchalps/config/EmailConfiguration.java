package eu.researchalps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.velocity.VelocityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

/**
 *
 */
@Configuration
@EnableConfigurationProperties(VelocityProperties.class)
public class EmailConfiguration {
    @Value("${email.enabled:false}")
    private boolean enabled;
    @Value("${email.user:}")
    private String user;
    @Value("${email.password:}")
    private String password;
    @Value("${email.tls:false}")
    private boolean tls;
    @Value("${email.host:localhost}")
    private String host;
    @Value("${email.port:25}")
    private int port;
    @Value("${email.sender:\"C-Radar\" <feedback@c-radar.com>}")
    private String sender;
    @Value("${email.recipients:feedback@c-radar.com}")
    private String recipients;

    @Bean
    public Session mailSession() {
        if (!enabled)
            return null;
        Properties props = new Properties();
        props.put("mail.smtp.auth", !user.isEmpty());
        props.put("mail.smtp.starttls.enable", tls);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        Authenticator authenticator = null;
        if (!user.isEmpty()) {
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            };
        }
        return Session.getDefaultInstance(props, authenticator);
    }

    public String getSender() {
        return sender;
    }

    public String getRecipients() {
        return recipients;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
