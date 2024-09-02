package com.woori.studylogin.Repository;

import com.woori.studylogin.Entity.BoardEntity;
import com.woori.studylogin.Entity.LikeEntity;
import com.woori.studylogin.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Integer> {
    Optional<LikeEntity> findByBoardAndUser(BoardEntity board, UserEntity user);
    void deleteByBoardAndUser(BoardEntity board, UserEntity user);
}
