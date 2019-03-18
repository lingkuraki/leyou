package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category>, SelectByIdListMapper<Category, Long> {

    /**
     * 根据品牌id查询商品分类
     *
     * @param bid 品牌id
     * @return
     */
    @Select("SELECT * FROM tb_category WHERE id IN (SELECT category_id FROM tb_category_brand WHERE brand_id=#{bid})")
    List<Category> queryByBrandId(Long bid);

    @Delete("DELETE FROM tb_category WHERE id = #{id}")
    int deleteByCategoryId(@Param("id") Long cid);

    @Update("UPDATE tb_category SET name=#{name} WHERE id = #{id}")
    int updateCategory(Category category);

    @Update("UPDATE tb_category SET is_Parent = #{isParent} WHERE id= #{id}")
    int updateIsParent(@Param("isParent") int isParent, @Param("id") Long parentId);

    @Select("SELECT parent_id FROM tb_category WHERE id = #{id}")
    Long selectParentIdById(@Param("id") Long cid);

    @Select("SELECT COUNT(id) FROM tb_category WHERE parent_id = #{parentId}")
    int countIdByParentId(@Param("parentId") Long parentId);
}
