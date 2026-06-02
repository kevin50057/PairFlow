package com.pairflow.question;

import com.pairflow.common.error.ApiException;
import com.pairflow.common.error.ErrorCode;
import com.pairflow.common.util.AppTime;
import com.pairflow.couple.Couple;
import com.pairflow.couple.CoupleContext;
import com.pairflow.question.dto.AnswerRequest;
import com.pairflow.question.dto.DailyQuestionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class QuestionService {

    private final QuestionCardRepository cardRepository;
    private final DailyQuestionRepository dailyRepository;
    private final QuestionAnswerRepository answerRepository;
    private final CoupleContext coupleContext;

    public QuestionService(QuestionCardRepository cardRepository,
                           DailyQuestionRepository dailyRepository,
                           QuestionAnswerRepository answerRepository,
                           CoupleContext coupleContext) {
        this.cardRepository = cardRepository;
        this.dailyRepository = dailyRepository;
        this.answerRepository = answerRepository;
        this.coupleContext = coupleContext;
    }

    @Transactional
    public DailyQuestionResponse today() {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        return toResponse(getOrCreateToday(couple.getId()), me, couple.partnerOf(me));
    }

    @Transactional
    public DailyQuestionResponse answer(AnswerRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        DailyQuestion dq = getOrCreateToday(couple.getId());

        QuestionAnswer answer = answerRepository.findByDailyQuestionIdAndUserId(dq.getId(), me)
                .orElseGet(QuestionAnswer::new);
        answer.setDailyQuestionId(dq.getId());
        answer.setUserId(me);
        answer.setAnswer(req.answer().trim());
        answerRepository.save(answer);

        return toResponse(dq, me, couple.partnerOf(me));
    }

    @Transactional(readOnly = true)
    public List<DailyQuestionResponse> history() {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        String partnerId = couple.partnerOf(me);
        return dailyRepository.findByCoupleIdOrderByDateDesc(couple.getId()).stream()
                .map(dq -> toResponse(dq, me, partnerId))
                .toList();
    }

    @Transactional
    public DailyQuestionResponse toggleFavorite(String dailyQuestionId) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        DailyQuestion dq = dailyRepository.findById(dailyQuestionId)
                .orElseThrow(() -> ApiException.notFound("Question not found"));
        if (!dq.getCoupleId().equals(couple.getId())) {
            throw ApiException.notFound("Question not found");
        }
        dq.setFavorite(!dq.isFavorite());
        return toResponse(dq, me, couple.partnerOf(me));
    }

    // ---- helpers ---------------------------------------------------------

    private DailyQuestion getOrCreateToday(String coupleId) {
        LocalDate today = AppTime.today();
        return dailyRepository.findByCoupleIdAndDate(coupleId, today).orElseGet(() -> {
            List<QuestionCard> all = cardRepository.findAll();
            if (all.isEmpty()) {
                throw new ApiException(ErrorCode.INTERNAL_ERROR, "Question catalog is empty");
            }
            // Stable per couple per day, varied across couples.
            int index = Math.floorMod((coupleId + ":" + today).hashCode(), all.size());
            DailyQuestion dq = new DailyQuestion();
            dq.setCoupleId(coupleId);
            dq.setQuestionCardId(all.get(index).getId());
            dq.setDate(today);
            return dailyRepository.save(dq);
        });
    }

    private DailyQuestionResponse toResponse(DailyQuestion dq, String me, String partnerId) {
        QuestionCard card = cardRepository.findById(dq.getQuestionCardId()).orElse(null);
        List<QuestionAnswer> answers = answerRepository.findByDailyQuestionId(dq.getId());

        QuestionAnswer mine = answers.stream().filter(a -> a.getUserId().equals(me)).findFirst().orElse(null);
        QuestionAnswer theirs = answers.stream().filter(a -> a.getUserId().equals(partnerId)).findFirst().orElse(null);

        boolean myAnswered = mine != null;
        boolean partnerAnswered = theirs != null;
        boolean both = myAnswered && partnerAnswered;

        return new DailyQuestionResponse(
                dq.getId(), dq.getDate(),
                card != null ? card.getText() : null,
                card != null ? card.getCategory() : null,
                card != null ? card.getSensitivity().name() : null,
                myAnswered ? mine.getAnswer() : null,
                both ? theirs.getAnswer() : null,   // partner answer gated until both answered
                myAnswered, partnerAnswered, both, dq.isFavorite());
    }
}
