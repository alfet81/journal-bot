package com.alfet.journalbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google-api")
public record GoogleApiProperties(String appName, String tokenDir, String credentialsFile,
                                  String userId) {
}
