# 第一阶段：构建应用
FROM maven:3.9-amazoncorretto-23 AS build
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 第二阶段：创建运行环境
FROM openjdk:23-jdk-slim
LABEL org.opencontainers.image.source=https://github.com/jihuayu/fake-llm
LABEL org.opencontainers.image.description="虚假的LLM提供商，用于调试和测试目的"
LABEL org.opencontainers.image.licenses=Apache-2.0
LABEL org.opencontainers.image.authors="jihuayu"

WORKDIR /app

# 从构建阶段复制构建好的JAR文件
COPY --from=build /app/target/*.jar app.jar

# 暴露应用端口（根据你的应用配置调整）
EXPOSE 8080

RUN useradd -ms /bin/bash myuser -u 101
RUN chown -R myuser:myuser /app
USER 101

# 设置容器启动命令
ENTRYPOINT ["java","-jar", "/app/app.jar"]