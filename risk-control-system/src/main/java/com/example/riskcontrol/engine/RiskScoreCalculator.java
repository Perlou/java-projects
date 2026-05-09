package com.example.riskcontrol.engine;

import com.example.riskcontrol.model.Alert;
import com.example.riskcontrol.model.UserRiskProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 风险评分计算器
 * 
 * 根据各种因素计算用户的风险分数
 */
@Component
public class RiskScoreCalculator {

    private static final Logger log = LoggerFactory.getLogger(RiskScoreCalculator.class);

    @Value("${riskcontrol.score.initial:50}")
    private int initialScore;

    @Value("${riskcontrol.score.max:100}")
    private int maxScore;

    @Value("${riskcontrol.score.min:0}")
    private int minScore;

    @PostConstruct
    public void init() {
        log.info("RiskScoreCalculator 初始化完成");
        log.info("  评分范围: [{}, {}]，初始分数: {}", minScore, maxScore, initialScore);
    }

    /**
     * 根据告警更新风险分数
     */
    public int calculateFromAlerts(UserRiskProfile profile, List<Alert> alerts) {
        int scoreChange = 0;

        for (Alert alert : alerts) {
            scoreChange += alert.getRiskScore();

            // 根据告警级别额外加分
            switch (alert.getLevel()) {
                case CRITICAL -> scoreChange += 10;
                case HIGH -> scoreChange += 5;
                case MEDIUM -> scoreChange += 2;
                case LOW -> scoreChange += 1;
            }
        }

        int newScore = Math.min(maxScore, Math.max(minScore,
                profile.getRiskScore() + scoreChange));

        log.debug("用户 {} 风险分数变化: {} → {} (变化: +{})",
                profile.getUserId(), profile.getRiskScore(), newScore, scoreChange);

        return newScore;
    }

    /**
     * 根据行为特征计算基础风险分数
     */
    public int calculateBaseScore(UserRiskProfile profile) {
        int score = initialScore;

        // 交易频率影响
        if (profile.getTransactionCount1h() > 10) {
            score += 10;
        }
        if (profile.getTransactionCount24h() > 50) {
            score += 15;
        }

        // 多城市交易
        if (profile.getRecentCities().size() > 3) {
            score += 10;
        }

        // 多设备使用
        if (profile.getRecentDevices().size() > 5) {
            score += 10;
        }

        // 登录失败次数
        if (profile.getLoginFailCount() > 0) {
            score += profile.getLoginFailCount() * 5;
        }

        // 历史告警
        if (profile.getAlertCount() > 0) {
            score += Math.min(profile.getAlertCount() * 3, 30);
        }

        return Math.min(maxScore, Math.max(minScore, score));
    }

    /**
     * 时间衰减
     * 长时间无异常行为，风险分数逐渐降低
     */
    public int applyTimeDecay(int currentScore, long hoursSinceLastAlert) {
        if (hoursSinceLastAlert > 24) {
            // 24小时后每天减少2分
            int daysAfter24h = (int) ((hoursSinceLastAlert - 24) / 24);
            int decay = Math.min(daysAfter24h * 2, currentScore - minScore);
            return currentScore - decay;
        }
        return currentScore;
    }
}
