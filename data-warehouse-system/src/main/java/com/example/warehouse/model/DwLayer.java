package com.example.warehouse.model;

/**
 * 数仓分层枚举
 * 
 * 标准数仓四层架构 + 维度层
 */
public enum DwLayer {

    /**
     * ODS - 原始数据层 (Operational Data Store)
     * 原始数据落地，不做处理
     */
    ODS("原始数据层", "数据源直接落地，保持原始格式"),

    /**
     * DWD - 明细数据层 (Data Warehouse Detail)
     * 清洗后的明细数据，关联维度
     */
    DWD("明细数据层", "数据清洗、维度退化后的明细数据"),

    /**
     * DWS - 服务数据层 (Data Warehouse Service)
     * 轻度汇总，面向主题域
     */
    DWS("服务数据层", "轻度汇总的宽表，按主题域组织"),

    /**
     * ADS - 应用数据层 (Application Data Service)
     * 面向应用的数据集市
     */
    ADS("应用数据层", "面向具体应用场景的数据集市"),

    /**
     * DIM - 维度表层 (Dimension)
     * 公共维度表
     */
    DIM("维度表层", "公共维度表，供各层引用");

    private final String displayName;
    private final String description;

    DwLayer(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
