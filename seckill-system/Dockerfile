# ==============================================
# 多阶段 Dockerfile
# Spring Boot 应用容器化最佳实践
# ==============================================

# 阶段 1: 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 1. 先复制依赖文件，利用 Docker 缓存层
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. 复制源码并构建
COPY src ./src
RUN mvn package -DskipTests -B

# 阶段 2: 运行阶段 (最小化镜像)
FROM eclipse-temurin:17-jre-alpine

# 创建非 root 用户 (安全最佳实践)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# 从构建阶段复制 JAR 包
COPY --from=builder /build/target/*.jar app.jar

# JVM 参数配置
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
