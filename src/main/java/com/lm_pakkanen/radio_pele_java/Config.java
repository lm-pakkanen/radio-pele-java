package com.lm_pakkanen.radio_pele_java;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

@Configuration
@ComponentScan("com.lm_pakkanen.radio_pele_java.controllers")
@PropertySource("classpath:application.yml")
public class Config {

  @Value("${BOT_STATUS_MESSAGE}")
  public String BOT_STATUS_MESSAGE;

  @Value("${BOT_CLIENT_ID}")
  public String BOT_CLIENT_ID;

  @Value("${BOT_TOKEN}")
  public String BOT_TOKEN;

  @Value("${SPOTIFY_CLIENT_ID}")
  public String SPOTIFY_CLIENT_ID;

  @Value("${SPOTIFY_CLIENT_SECRET}")
  public String SPOTIFY_CLIENT_SECRET;

  @Bean
  public JDA getJDAInstance() {
    JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN);
    builder.setStatus(OnlineStatus.ONLINE);
    builder.setActivity(Activity.playing(BOT_STATUS_MESSAGE));

    JDA jda = builder.build();
    return jda;
  }

}
