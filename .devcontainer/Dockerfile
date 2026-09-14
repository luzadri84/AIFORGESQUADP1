FROM eclipse-temurin:21.0.10_7-jdk-jammy@sha256:25d1276565738d3c805e632a4542c3a7598866ef967f4def6544c15de3a74b14 AS jdk
FROM node:22.22.0-bookworm@sha256:20a424ecd1d2064a44e12fe287bf3dae443aab31dc5e0c0cb6c74bef9c78911c
COPY --from=jdk /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="/opt/java/openjdk/bin:${PATH}"
RUN java -version && node --version && npm --version && git --version \
    && mkdir -p /home/node/.m2 /home/node/.npm /workspace/infra/checks/frontend/node_modules \
    && chown -R node:node /home/node/.m2 /home/node/.npm /workspace
USER node
WORKDIR /workspace
CMD ["sleep", "infinity"]