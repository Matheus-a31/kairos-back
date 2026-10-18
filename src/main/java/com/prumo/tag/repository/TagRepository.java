package com.prumo.tag.repository;

import com.prumo.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByProjectId(Long projectId);
    Optional<Tag> findByProjectIdAndName(Long projectId, String name);
}


