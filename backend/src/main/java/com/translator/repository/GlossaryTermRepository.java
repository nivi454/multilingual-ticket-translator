package com.translator.repository;

import com.translator.entity.GlossaryTermEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlossaryTermRepository extends JpaRepository<GlossaryTermEntity, Long> {

    List<GlossaryTermEntity> findBySourceLanguageAndTargetLanguage(String sourceLanguage, String targetLanguage);

    Optional<GlossaryTermEntity> findFirstBySourceLanguageAndTargetLanguageAndSourceTermIgnoreCase(
            String sourceLanguage, String targetLanguage, String sourceTerm
    );

    List<GlossaryTermEntity> findAllByOrderBySourceLanguageAscSourceTermAsc();
}
