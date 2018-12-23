package com.github.cherrythefatbunny;

import com.github.cherrythefatbuny.Ds;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

//ds not specified in application.properties
@Ds("ds22")
@Mapper
public interface PersonMapperC {
    @Select("select name from person where id = #{id}")
    String getName(String id);
}
