package com.monitoring.monitor.service;

import com.monitoring.monitor.model.entities.UserEntity;
import com.monitoring.monitor.model.interfaces.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jvnet.hk2.annotations.Service;

@Service
@AllArgsConstructor
@Log4j2
public class UserService {

    private final UserRepository userRepository;

    public void addUserToDatabase(User user){
        try{
            UserEntity userEntity = new UserEntity();
            if(userRepository.findByUsername(user.getUserName())!=null){
                System.out.println("Уже существует!");
            }else{
                userEntity.setUsername(user.getUserName());
                userRepository.save(userEntity);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public List<UserEntity> getAllUsers(){
        return userRepository.findAll();
    }
}
