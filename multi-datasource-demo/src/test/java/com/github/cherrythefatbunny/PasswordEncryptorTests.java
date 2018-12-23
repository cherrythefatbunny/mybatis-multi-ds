package com.github.cherrythefatbunny;

import org.jasypt.encryption.StringEncryptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PasswordEncryptorTests {
    @Autowired
    StringEncryptor stringEncryptor;

    @Test
    public void encrypt() {
        String result = stringEncryptor.encrypt("postgres");
        System.out.println("==================");
        System.out.println(result);
        System.out.println("==================");
    }
}

