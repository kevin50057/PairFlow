package com.pairflow.config;

import com.pairflow.anniversary.Anniversary;
import com.pairflow.anniversary.AnniversaryRepository;
import com.pairflow.anniversary.RepeatType;
import com.pairflow.common.enums.Priority;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.Couple;
import com.pairflow.couple.CoupleRepository;
import com.pairflow.couple.CoupleStatus;
import com.pairflow.mood.MoodEntry;
import com.pairflow.mood.MoodEntryRepository;
import com.pairflow.mood.MoodType;
import com.pairflow.todo.Todo;
import com.pairflow.todo.TodoRepository;
import com.pairflow.todo.TodoType;
import com.pairflow.user.Gender;
import com.pairflow.user.User;
import com.pairflow.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds a demo couple (Kevin + 魚丸) with a little content on a fresh database, so the
 * app is explorable immediately. Runs only when no users exist. Disable with
 * {@code pairflow.seed-demo=false}.
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "pairflow.seed-demo", havingValue = "true", matchIfMissing = true)
public class DevSeeder implements ApplicationRunner {

    private static final LocalDate DEMO_RELATIONSHIP_START = LocalDate.of(2026, 6, 10);

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final AnniversaryRepository anniversaryRepository;
    private final TodoRepository todoRepository;
    private final MoodEntryRepository moodRepository;
    private final PasswordEncoder passwordEncoder;

    public DevSeeder(UserRepository userRepository, CoupleRepository coupleRepository,
                     AnniversaryRepository anniversaryRepository, TodoRepository todoRepository,
                     MoodEntryRepository moodRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.coupleRepository = coupleRepository;
        this.anniversaryRepository = anniversaryRepository;
        this.todoRepository = todoRepository;
        this.moodRepository = moodRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("Seeding demo couple (kevin@pairflow.test / fishBall@pairflow.test, password: secret123)");

        User kevin = user("kevin@pairflow.test", "Kevin");
        kevin.setBirthday(LocalDate.of(1996, 4, 14));
        kevin.setGender(Gender.MALE);
        kevin.setAvatarUrl("/avatars/boy.png");
        kevin.setBio("愛吃、愛睡、愛你💻");

        User fishBall = user("fishBall@pairflow.test", "魚丸");
        fishBall.setBirthday(LocalDate.of(1997, 7, 10));
        fishBall.setGender(Gender.FEMALE);
        fishBall.setAvatarUrl("/avatars/girl.png");
        fishBall.setBio(" 🐟");

        userRepository.save(kevin);
        userRepository.save(fishBall);

        Couple couple = new Couple();
        couple.setUserAId(kevin.getId());
        couple.setUserBId(fishBall.getId());
        couple.setStatus(CoupleStatus.ACTIVE);
        couple.setRelationshipStartDate(DEMO_RELATIONSHIP_START);
        couple = coupleRepository.save(couple);
        String coupleId = couple.getId();

        // 生日改由個人檔案的 birthday 欄位自動帶入倒數與通知，不再手動建立紀念日。
        anniversaryRepository.save(anniversary(coupleId, kevin.getId(), "交往紀念日", DEMO_RELATIONSHIP_START));

        Todo todo = new Todo();
        todo.setCoupleId(coupleId);
        todo.setTitle("回家路上買晚餐");
        todo.setType(TodoType.GENERAL);
        todo.setPriority(Priority.MEDIUM);
        todo.setAssignedToBoth(true);
        todo.setCreatedBy(kevin.getId());
        todo.setDueDate(LocalDate.now(AppTime.ZONE).atTime(18, 0).atZone(AppTime.ZONE).toInstant());
        todoRepository.save(todo);

        MoodEntry mood = new MoodEntry();
        mood.setCoupleId(coupleId);
        mood.setUserId(fishBall.getId());
        mood.setMood(MoodType.TIRED);
        mood.setEmoji("😮‍💨");
        mood.setNote("今天有點累");
        mood.setNeedResponse(true);
        moodRepository.save(mood);
    }

    private User user(String email, String name) {
        User u = new User();
        u.setEmail(email);
        u.setDisplayName(name);
        u.setPasswordHash(passwordEncoder.encode("secret123"));
        u.setTimezone("Asia/Taipei");
        return u;
    }

    private Anniversary anniversary(String coupleId, String createdBy, String title, LocalDate date) {
        Anniversary a = new Anniversary();
        a.setCoupleId(coupleId);
        a.setTitle(title);
        a.setDate(date);
        a.setRepeatType(RepeatType.YEARLY);
        a.setReminderDaysBeforeCsv("30,7,1,0");
        a.setCreatedBy(createdBy);
        return a;
    }
}
