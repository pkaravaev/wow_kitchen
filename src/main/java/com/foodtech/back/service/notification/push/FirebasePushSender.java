package com.foodtech.back.service.notification.push;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.entity.auth.FirebaseToken;
import com.foodtech.back.repository.auth.FirebaseTokenRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class FirebasePushSender {

    private final ResourceLoader resourceLoader;

    private static final String SERVICE_ACCOUNT_KEY_PATH = "serviceAccountKey.json";

    private FirebaseMessaging firebaseMessaging;

    private final FirebaseTokenRepository firebaseTokenRepository;

    private final ResourcesProperties properties;

    public FirebasePushSender(FirebaseTokenRepository firebaseTokenRepository, ResourcesProperties properties, ResourceLoader resourceLoader) {
        this.firebaseTokenRepository = firebaseTokenRepository;
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init(){

        try {
            Resource resource = resourceLoader.getResource("classpath:serviceAccountKey.json");
            ClassLoader classLoader = getClass().getClassLoader();
           // URL resource = classLoader.getResource(SERVICE_ACCOUNT_KEY_PATH);
            if (Objects.isNull(resource)) {
                throw new IllegalArgumentException("Can't obtain firebase service account key resource");
            }
           // File file = new File(resource.toURI());
           // FileInputStream input = new FileInputStream(file);
            InputStream file = resource.getInputStream();
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(file);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(googleCredentials).build();
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
            firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);

        } catch (Exception e){
            log.error("Firebase push sender initialization error", e);
        }
    }

    void send(Long userId, String body) {

        Optional<FirebaseToken> firebaseTokenOpt = firebaseTokenRepository.findByUserId(userId);
        if (firebaseTokenOpt.isEmpty()) {
            log.info("No firebase token found. User: '{}'", userId);
            return;
        }

        String result = sendPushByToken(firebaseTokenOpt.get().getToken(), body);
        if (Objects.nonNull(result)) {
            log.info("Push '{}' successfully sent. User: '{}'", body, userId);
        }
    }

    public String sendPushByToken(String firebaseToken, String body) {
        String result = null;

        try {
            Message fireBaseMessage = Message.builder()
                    .setNotification(new Notification(properties.getPushTitle(), body))
                    .setToken(firebaseToken)
                    .build();
            result = firebaseMessaging.send(fireBaseMessage);

        } catch (FirebaseMessagingException exception){
            log.error("Sending push notification failed. Cause: '{}'", exception.getMessage());
        }

        return result;
    }
}
