package com.github.cherrythefatbunny;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiDatasourceDemoApplicationTests {
    @Autowired
    PersonMapperA personMapperA;
    @Autowired
    PersonMapperB personMapperB;
    @Autowired
    PersonMapperC personMapperC;
    @Test
    @Repeat(10)
    public void personCase() {
        Assert.assertEquals("name1", personMapperA.getName("111"));
        Assert.assertEquals("name2", personMapperB.getName("111"));
        Assert.assertEquals("name1", personMapperC.getName("111"));
    }
}

