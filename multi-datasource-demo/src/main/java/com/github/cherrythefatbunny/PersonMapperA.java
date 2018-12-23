package com.github.cherrythefatbunny;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PersonMapperA {
    @Select("select name from person where id = #{id}")
    String getName(String id);
}
