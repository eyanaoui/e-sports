package com.esports.services;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for generating and validating CAPTCHA challenges.
 * 
 * Generates simple mathematical or text-based CAPTCHAs to prevent
 * automated bot attacks on login, registration, and password recovery.
 * 
 * Features:
 * - Mathematical challenges (e.g., "5 + 3 = ?")
 * - Text-based challenges with distortion
 * - Session-based validation
 * - Automatic expiration (5 minutes)
 */
public class CaptchaService {
    
    private static final SecureRandom random = new SecureRandom();
    private static final Map<String, CaptchaChallenge> activeChallenges = new HashMap<>();
    private static final int CAPTCHA_EXPIRY_MINUTES = 5;
    
    /**
     * CAPTCHA types.
     */
    public enum CaptchaType {
        MATH,      // Mathematical challenge (e.g., "5 + 3 = ?")
        TEXT       // Text-based challenge (e.g., "Enter: ABC123")
    }
    
    /**
     * Generate a new CAPTCHA challenge.
     * 
     * @param type The type of CAPTCHA to generate
     * @return CaptchaChallenge containing the challenge ID, question, and answer
     */
    public CaptchaChallenge generateCaptcha(CaptchaType type) {
        String challengeId = UUID.randomUUID().toString();
        String question;
        String answer;
        
        switch (type) {
            case MATH:
                int num1 = random.nextInt(10) + 1;  // 1-10
                int num2 = random.nextInt(10) + 1;  // 1-10
                int operation = random.nextInt(3);  // 0=add, 1=subtract, 2=multiply
                
                switch (operation) {
                    case 0: // Addition
                        question = num1 + " + " + num2 + " = ?";
                        answer = String.valueOf(num1 + num2);
                        break;
                    case 1: // Subtraction (ensure positive result)
                        if (num1 < num2) {
                            int temp = num1;
                            num1 = num2;
                            num2 = temp;
                        }
                        question = num1 + " - " + num2 + " = ?";
                        answer = String.valueOf(num1 - num2);
                        break;
                    case 2: // Multiplication (smaller numbers)
                        num1 = random.nextInt(5) + 1;  // 1-5
                        num2 = random.nextInt(5) + 1;  // 1-5
                        question = num1 + " × " + num2 + " = ?";
                        answer = String.valueOf(num1 * num2);
                        break;
                    default:
                        question = num1 + " + " + num2 + " = ?";
                        answer = String.valueOf(num1 + num2);
                }
                break;
                
            case TEXT:
            default:
                // Generate random alphanumeric string (6 characters)
                String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Exclude confusing chars
                StringBuilder code = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    code.append(chars.charAt(random.nextInt(chars.length())));
                }
                answer = code.toString();
                question = "Enter: " + answer;
                break;
        }
        
        CaptchaChallenge challenge = new CaptchaChallenge(challengeId, question, answer, type);
        
        // Store challenge with expiry
        activeChallenges.put(challengeId, challenge);
        
        // Clean up expired challenges
        cleanupExpiredChallenges();
        
        return challenge;
    }
    
    /**
     * Validate a CAPTCHA response.
     * 
     * @param challengeId The challenge ID
     * @param userAnswer The user's answer
     * @return true if answer is correct and challenge is valid
     */
    public boolean validateCaptcha(String challengeId, String userAnswer) {
        if (challengeId == null || userAnswer == null) {
            return false;
        }
        
        CaptchaChallenge challenge = activeChallenges.get(challengeId);
        
        if (challenge == null) {
            System.out.println("[CAPTCHA] Challenge not found or expired: " + challengeId);
            return false;
        }
        
        // Check if expired
        if (challenge.isExpired()) {
            activeChallenges.remove(challengeId);
            System.out.println("[CAPTCHA] Challenge expired: " + challengeId);
            return false;
        }
        
        // Validate answer (case-insensitive for text)
        boolean isValid = challenge.getAnswer().equalsIgnoreCase(userAnswer.trim());
        
        // Remove challenge after validation (one-time use)
        activeChallenges.remove(challengeId);
        
        if (isValid) {
            System.out.println("[CAPTCHA] Challenge validated successfully: " + challengeId);
        } else {
            System.out.println("[CAPTCHA] Invalid answer for challenge: " + challengeId);
        }
        
        return isValid;
    }
    
    /**
     * Clean up expired challenges.
     */
    private void cleanupExpiredChallenges() {
        activeChallenges.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Get the number of active challenges (for monitoring).
     */
    public int getActiveChallengeCount() {
        cleanupExpiredChallenges();
        return activeChallenges.size();
    }
    
    /**
     * CAPTCHA challenge data.
     */
    public static class CaptchaChallenge {
        private final String challengeId;
        private final String question;
        private final String answer;
        private final CaptchaType type;
        private final long createdAt;
        
        public CaptchaChallenge(String challengeId, String question, String answer, CaptchaType type) {
            this.challengeId = challengeId;
            this.question = question;
            this.answer = answer;
            this.type = type;
            this.createdAt = System.currentTimeMillis();
        }
        
        public String getChallengeId() {
            return challengeId;
        }
        
        public String getQuestion() {
            return question;
        }
        
        public String getAnswer() {
            return answer;
        }
        
        public CaptchaType getType() {
            return type;
        }
        
        public boolean isExpired() {
            long ageMinutes = (System.currentTimeMillis() - createdAt) / (1000 * 60);
            return ageMinutes >= CAPTCHA_EXPIRY_MINUTES;
        }
        
        @Override
        public String toString() {
            return "CaptchaChallenge{" +
                    "challengeId='" + challengeId + '\'' +
                    ", question='" + question + '\'' +
                    ", type=" + type +
                    ", expired=" + isExpired() +
                    '}';
        }
    }
}
