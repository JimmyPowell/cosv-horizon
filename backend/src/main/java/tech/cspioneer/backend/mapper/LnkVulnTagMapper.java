package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface LnkVulnTagMapper {

    @Insert("INSERT INTO lnk_vulnerability_metadata_tag(vulnerability_metadata_id, tag_id) VALUES(#{vmId}, #{tagId}) ON DUPLICATE KEY UPDATE tag_id = VALUES(tag_id)")
    int link(@Param("vmId") Long vmId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM lnk_vulnerability_metadata_tag WHERE vulnerability_metadata_id = #{vmId} AND tag_id = #{tagId}")
    int unlink(@Param("vmId") Long vmId, @Param("tagId") Long tagId);

    @Select("SELECT COUNT(1) FROM lnk_vulnerability_metadata_tag WHERE tag_id = #{tagId}")
    long countByTagId(@Param("tagId") Long tagId);

    @Delete("DELETE FROM lnk_vulnerability_metadata_tag WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);

    @Update("UPDATE lnk_vulnerability_metadata_tag SET tag_id = #{newTagId} WHERE tag_id = #{oldTagId}")
    int remapTag(@Param("oldTagId") Long oldTagId, @Param("newTagId") Long newTagId);

    @Delete({
            "<script>",
            "DELETE l1 FROM lnk_vulnerability_metadata_tag l1",
            "JOIN lnk_vulnerability_metadata_tag l2 ON l1.vulnerability_metadata_id = l2.vulnerability_metadata_id",
            "WHERE l1.tag_id = #{oldTagId} AND l2.tag_id = #{newTagId}",
            "</script>"
    })
    int deleteDuplicatesForMigration(@Param("oldTagId") Long oldTagId, @Param("newTagId") Long newTagId);

    @Delete("DELETE FROM lnk_vulnerability_metadata_tag WHERE vulnerability_metadata_id = #{vmId}")
    int deleteByVulnerabilityId(@Param("vmId") Long vmId);
}
