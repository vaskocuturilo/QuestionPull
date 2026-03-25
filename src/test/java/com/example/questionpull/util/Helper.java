package com.example.questionpull.util;

import com.example.questionpull.entity.DataVersionEntity;
import com.example.questionpull.entity.QuestionEntity;
import com.example.questionpull.entity.SolutionEntity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Helper {
    public static DataVersionEntity buildVersion(String checksum) {
        DataVersionEntity v = new DataVersionEntity();
        v.setDataKey("questions");
        v.setChecksum(checksum);
        v.setUpdatedAt(LocalDateTime.now());
        return v;
    }

    public static List<QuestionEntity> buildQuestionList() {
        QuestionEntity question = new QuestionEntity();
        question.setTitle("Title1");
        question.setBody("Body1");
        question.setExample("Example1");
        question.setLevel("easy");

        SolutionEntity solution = new SolutionEntity();
        solution.setContent("Use streams...");
        question.setSolution(solution);

        return new ArrayList<>(List.of(question));
    }

    public static String computeChecksum(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
