package com.github.cherrythefatbunny;

import com.github.cherrythefatbuny.Ds;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Ds("ds2")
@Mapper
public interface PersonMapperB {
    @Select("select name from person where id = #{id}")
    String getName(String id);
}
