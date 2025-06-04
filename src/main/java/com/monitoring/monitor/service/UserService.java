package com.monitoring.monitor.service;

import com.monitoring.monitor.model.entities.UserEntity;
import com.monitoring.monitor.model.interfaces.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
@Log4j2
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void addUserToDatabase(User user) {
        try {
            UserEntity userEntity = new UserEntity();
            if (userRepository.findByUsername(user.getUserName()) != null) {
                System.out.println("Уже существует!");
            } else {
                userEntity.setUsername(user.getUserName());
                userRepository.save(userEntity);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
    }