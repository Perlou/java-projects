package com.example.chatbot.rag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * FAQ 知识库加载器
 * 
 * 加载和管理常见问题知识库
 * 用于简化版 RAG 实现
 */
public class KnowledgeLoader {

        // FAQ 知识库
        private final List<FaqEntry> faqEntries = new ArrayList<>();

        public KnowledgeLoader() {
                loadDefaultFaqs();
        }

        /**
         * 加载默认 FAQ 数据
         */
        private void loadDefaultFaqs() {
                // 退货政策
                addFaq(
                                Arrays.asList("退货政策", "退货", "退货规则", "怎么退货", "可以退货吗"),
                                """
                                                【退货政策】
                                                1. 商品签收后7天内可申请无理由退货
                                                2. 退货商品需保持原包装完好，附件齐全
                                                3. 电子产品若已激活/使用则不支持无理由退货
                                                4. 定制商品、贴身衣物等特殊商品不支持退货
                                                5. 退款将在审核通过后3-5个工作日内原路返还
                                                """);

                // 退款流程
                addFaq(
                                Arrays.asList("退款流程", "退款", "怎么退款", "退款多久到账"),
                                """
                                                【退款流程】
                                                1. 在订单详情页点击"申请退款"
                                                2. 选择退款原因并提交申请
                                                3. 客服会在1-3个工作日内审核
                                                4. 审核通过后，退款将在3-5个工作日内原路返还
                                                5. 若已发货，需先完成退货后才能退款
                                                """);

                // 换货政策
                addFaq(
                                Arrays.asList("换货", "换货政策", "怎么换货", "可以换货吗"),
                                """
                                                【换货政策】
                                                1. 商品签收后7天内可申请换货
                                                2. 换货商品需保持原包装完好
                                                3. 仅支持换同款不同颜色/尺寸
                                                4. 换货运费：质量问题由我们承担，其他原因由客户承担
                                                5. 换货时效：收到退回商品后3个工作日内发出新商品
                                                """);

                // 配送说明
                addFaq(
                                Arrays.asList("配送", "发货", "快递", "多久发货", "配送时间"),
                                """
                                                【配送说明】
                                                1. 普通商品：下单后24小时内发货
                                                2. 定制商品：根据商品页面说明
                                                3. 配送范围：全国（港澳台及偏远地区除外）
                                                4. 配送时效：
                                                   - 一线城市：1-2天
                                                   - 二三线城市：2-4天
                                                   - 其他地区：3-7天
                                                5. 配送方式：顺丰快递
                                                """);

                // 支付方式
                addFaq(
                                Arrays.asList("支付", "付款", "支付方式", "怎么付款"),
                                """
                                                【支付方式】
                                                支持以下支付方式：
                                                1. 支付宝
                                                2. 微信支付
                                                3. 银联卡
                                                4. 信用卡（Visa/MasterCard/银联）
                                                5. 花呗分期（部分商品支持）
                                                6. Apple Pay
                                                """);

                // 发票问题
                addFaq(
                                Arrays.asList("发票", "开发票", "电子发票", "发票怎么开"),
                                """
                                                【发票说明】
                                                1. 我们提供电子发票，订单完成后自动发送至您的邮箱
                                                2. 如需纸质发票，请在下单时备注
                                                3. 发票内容：商品明细或商品类别
                                                4. 发票抬头：支持个人或企业
                                                5. 如需补开发票，请联系客服
                                                """);

                // 会员权益
                addFaq(
                                Arrays.asList("会员", "会员权益", "积分", "会员等级"),
                                """
                                                【会员权益】
                                                银卡会员（消费满1000）：
                                                - 9.8折购物优惠
                                                - 生日双倍积分

                                                金卡会员（消费满5000）：
                                                - 9.5折购物优惠
                                                - 优先发货
                                                - 专属客服

                                                钻石会员（消费满20000）：
                                                - 9折购物优惠
                                                - 免费顺丰包邮
                                                - 新品优先购买权
                                                """);

                // 售后服务
                addFaq(
                                Arrays.asList("售后", "保修", "质保", "售后服务", "维修"),
                                """
                                                【售后服务】
                                                1. 电子产品：享受官方保修政策
                                                   - 手机/平板：1年保修
                                                   - 耳机/配件：6个月保修
                                                   - 电脑：1年保修
                                                2. 人为损坏不在保修范围内
                                                3. 保修期外可提供付费维修服务
                                                4. 如需维修，可联系在线客服预约
                                                """);

                // 客服联系方式
                addFaq(
                                Arrays.asList("联系客服", "客服电话", "人工客服", "投诉", "客服"),
                                """
                                                【联系我们】
                                                1. 在线客服：7x24小时在线
                                                2. 客服热线：400-888-8888（9:00-21:00）
                                                3. 客服邮箱：support@example.com
                                                4. 投诉建议：complaint@example.com
                                                5. 官方微信：搜索"智能客服"
                                                """);

                // 隐私政策
                addFaq(
                                Arrays.asList("隐私", "隐私政策", "个人信息", "数据安全"),
                                """
                                                【隐私政策】
                                                我们重视您的隐私：
                                                1. 仅收集必要的个人信息用于订单处理
                                                2. 不会向第三方出售您的信息
                                                3. 使用SSL加密保护数据传输
                                                4. 您可随时要求删除个人数据
                                                5. 详细政策请访问官网隐私政策页面
                                                """);
        }

        /**
         * 添加 FAQ 条目
         */
        public void addFaq(List<String> keywords, String answer) {
                faqEntries.add(new FaqEntry(keywords, answer));
        }

        /**
         * 获取所有 FAQ
         */
        public List<FaqEntry> getAllFaqs() {
                return Collections.unmodifiableList(faqEntries);
        }

        /**
         * FAQ 条目
         */
        public static class FaqEntry {
                private final List<String> keywords;
                private final String answer;

                public FaqEntry(List<String> keywords, String answer) {
                        this.keywords = keywords;
                        this.answer = answer;
                }

                public List<String> getKeywords() {
                        return keywords;
                }

                public String getAnswer() {
                        return answer;
                }
        }
}
