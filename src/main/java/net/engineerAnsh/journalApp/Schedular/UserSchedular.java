package net.engineerAnsh.journalApp.Schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.UserRepositoryImpl;
import net.engineerAnsh.journalApp.Service.EmailService;
import net.engineerAnsh.journalApp.enums.Mood;
import net.engineerAnsh.journalApp.model.SentimentData;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev")
@Slf4j
@RequiredArgsConstructor
public class UserSchedular {

    private final UserRepositoryImpl userRepository;
    private final EmailService emailService;
    private final KafkaTemplate<String ,SentimentData> kafkaTemplate;

    @Scheduled(cron = "0 0 9 * * SUN")
    public void fetchUsersAndSendSaMail() {
        List<User> Users = userRepository.getUserForSA();
        for(User user : Users){
            List<Journal> journalEntries = user.getJournals();
            List<Mood> moodList = journalEntries.stream()
                    .filter(x -> x.getCreatedAt()
                            .isAfter(LocalDateTime.now()
                                    .minus(7, ChronoUnit.DAYS)))
                    .map(x -> x.getMood())
                    .toList();

            // We will be mapping all the sentiments present in the journal entries with their frequency into a map...
            Map<Mood,Integer> mppOfSentiments = new HashMap<>();
            for(Mood mood : moodList){
                if(mood != null){
                    mppOfSentiments.put(mood,mppOfSentiments.getOrDefault(mood,0)+1);
                }
            }

            // We will be storing the mood that has maximum frequency the user's journal entries...
            int maxCount = 0;
            Mood mostFrequentMood = null;
            for(Map.Entry<Mood, Integer> sentimentInMap : mppOfSentiments.entrySet()){
                if(sentimentInMap.getValue() > maxCount){
                    maxCount = sentimentInMap.getValue();
                    mostFrequentMood = sentimentInMap.getKey();
                }
            }

            // we are sending mail to the user about their highest frequency mood...
            if(mostFrequentMood != null){
                // emailService.sendingEmail(user.getEmail(), "Sending the mood for last 7 days", mostFrequentMood.toString());

                SentimentData sentimentData = SentimentData.builder().email(user.getEmail()).sentiment("Mood for last 7 days " + mostFrequentMood).build();

                // We will be sending mail synchronously if kafka throws an error...
                try {
                    kafkaTemplate.send("weekly-sentiments",sentimentData.getEmail(),sentimentData);
                    System.out.println("Serializer: " + kafkaTemplate);
                } catch (Exception e) {
                    log.info("kafka is not active, mail is send synchronously...");
                    emailService.sendingEmail(sentimentData.getEmail(),"Mood for last 7 days ", sentimentData.getSentiment());
                }
            }
        }
    }

}
