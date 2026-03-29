//package com.example.badrock.advisor;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class TradeQnAAdvisor implements QuestionAndAnswerAdvisor {
//
//    @Autowired
//    private TradeService tradeService;
//
//    @Autowired
//    private ChatContext chatContext;
//
//    @Override
//    public QuestionAndAnswer advise(QuestionAndAnswer qna) {
//
//        String question = qna.getQuestion();
//        String answer = qna.getAnswer();
//
//        // Example: detect if trade failed
//        if (answer != null && answer.contains("FAILED") && chatContext.getTradeId() != null) {
//
//            // Suggest follow-up question
//            qna.setFollowUpQuestion("Do you want to replay the trade from the failed step?");
//        }
//
//        // You can also inject additional context here
//        // For example, append failed steps info
//        if (question.toLowerCase().contains("failed steps")) {
//            String failedSteps = tradeService.getFailedSteps(chatContext.getTradeId());
//            qna.setAnswer(answer + "\n\nFailed steps:\n" + failedSteps);
//        }
//
//        return qna;
//    }
//}