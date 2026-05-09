#!/bin/bash

# 实时数据分析平台启动脚本
# 
# 注意：由于 Spark 3.5 与 Java 17+ 存在兼容性问题
# 建议使用 Java 11 或 Java 17 运行此项目
#
# 如果您使用的是 Java 18+，可能会遇到以下错误：
# "getSubject is not supported"
#
# 解决方案：
# 1. 安装 Java 11 或 Java 17 (推荐使用 SDKMAN! 管理多版本)
# 2. 使用 JAVA_HOME 指定 Java 版本运行

echo "=== 实时数据分析平台 ==="
echo ""

# 检查 Java 版本
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "检测到 Java 版本: $JAVA_VERSION"

if [ "$JAVA_VERSION" -ge 18 ]; then
    echo ""
    echo "⚠️  警告: 检测到 Java $JAVA_VERSION"
    echo "   Spark 3.5 在 Java 18+ 上可能遇到兼容性问题"
    echo "   建议使用 Java 11 或 Java 17"
    echo ""
    echo "   如果您有 SDKMAN! 可以运行："
    echo "   sdk use java 17.x.x-tem"
    echo ""
fi

# 设置 JVM 参数以最大化兼容性
export JAVA_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.net=ALL-UNNAMED \
--add-opens=java.base/java.nio=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED \
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens=java.base/sun.nio.cs=ALL-UNNAMED \
--add-opens=java.base/sun.security.action=ALL-UNNAMED \
--add-opens=java.base/sun.util.calendar=ALL-UNNAMED \
--add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED \
-Djdk.reflect.useDirectMethodHandle=false"

echo "启动应用..."
mvn spring-boot:run -Dspring-boot.run.jvmArguments="$JAVA_OPTS"
