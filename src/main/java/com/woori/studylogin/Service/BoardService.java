package com.woori.studylogin.Service;

import com.woori.studylogin.Constant.CategoryType;
import com.woori.studylogin.DTO.BoardDTO;
import com.woori.studylogin.Entity.BoardEntity;
import com.woori.studylogin.Entity.LikeEntity;
import com.woori.studylogin.Entity.UserEntity;
import com.woori.studylogin.Repository.BoardRepository;
import com.woori.studylogin.Repository.LikeRepository;
import com.woori.studylogin.Repository.UserRepository;
import com.woori.studylogin.Util.FileUpload;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class BoardService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;
    private final FileUpload fileUpload;

    @Value("${imgUploadLocation}")
    private String imgUploadLocation;

    // 삽입
    public void save(BoardDTO boardDTO, MultipartFile file) throws IOException {
        boardDTO.setLikeCount(0);
        boardDTO.setViewCount(0);

        String newFileName = "";
        if (!file.isEmpty()) {
            newFileName = fileUpload.upload(file, imgUploadLocation);
        }
        boardDTO.setBoardImg(newFileName);

        BoardEntity boardEntity = modelMapper.map(boardDTO, BoardEntity.class);
        boardRepository.save(boardEntity);
    }

    // 수정
    public void update(BoardDTO boardDTO, MultipartFile file) throws IOException {
        BoardEntity boardEntity = boardRepository.findById(boardDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("No board found with id: " + boardDTO.getId()));

        String deleteFile = boardEntity.getBoardImg();
        String newFileName = "";

        if (!file.isEmpty()) {
            if (deleteFile != null && !deleteFile.isEmpty()) {
                fileUpload.deleteFile(deleteFile, imgUploadLocation);
            }
            newFileName = fileUpload.upload(file, imgUploadLocation);
            boardDTO.setBoardImg(newFileName);
        }

        modelMapper.map(boardDTO, boardEntity);
        boardRepository.save(boardEntity);
    }

    // 삭제
    public void delete(Integer id) throws IOException {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No board found with id: " + id));

        fileUpload.deleteFile(boardEntity.getBoardImg(), imgUploadLocation);
        boardRepository.deleteById(id);
    }

    // 개별 조회
    public BoardDTO findById(Integer id, String pandan) {
        if (pandan.equals("R")) {
            boardRepository.viewcnt(id);
        }
        return boardRepository.findById(id)
                .map(boardEntity -> modelMapper.map(boardEntity, BoardDTO.class))
                .orElseThrow(() -> new IllegalArgumentException("No board found with id: " + id));
    }

    // 전체 조회
    public Page<BoardDTO> list(String searchType, String searchKeyword, String category, Pageable pageable) {
        CategoryType categoryType = category.isEmpty() ? null : CategoryType.valueOf(category.toUpperCase());

        // Pageable 객체를 regDate 기준으로 내림차순 정렬하도록 설정
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "regDate"));

        Page<BoardEntity> read;

        if (searchType.equals("title")) {
            read = (categoryType == null)
                    ? boardRepository.findByTitleContaining(searchKeyword, sortedPageable)
                    : boardRepository.findByTitleContainingAndCategory(searchKeyword, categoryType, sortedPageable);
        } else if (searchType.equals("content")) {
            read = (categoryType == null)
                    ? boardRepository.findByContentContaining(searchKeyword, sortedPageable)
                    : boardRepository.findByContentContainingAndCategory(searchKeyword, categoryType, sortedPageable);
        } else if (searchType.equals("titleContent")) {
            read = (categoryType == null)
                    ? boardRepository.findByTitleContainingOrContentContaining(searchKeyword, searchKeyword, sortedPageable)
                    : boardRepository.findByTitleContainingOrContentContainingAndCategory(searchKeyword, searchKeyword, categoryType, sortedPageable);
        } else {
            read = (categoryType == null)
                    ? boardRepository.findAll(sortedPageable)
                    : boardRepository.findByCategory(categoryType, sortedPageable);
        }

        return read.map(data -> modelMapper.map(data, BoardDTO.class));
    }

    // 추천 수 증가
    @Transactional
    public void increaseLikeCount(Integer boardId, String username) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("No board found with id: " + boardId));
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No user found with username: " + username));

        // 이미 추천했는지 여부 확인
        if (likeRepository.findByBoardAndUser(board, user).isPresent()) {
            throw new IllegalStateException("Already liked the post.");
        }

        // 추천 추가
        LikeEntity like = new LikeEntity();
        like.setBoard(board);
        like.setUser(user);
        likeRepository.save(like);

        boardRepository.increaseLikeCount(boardId);
    }

    // 추천 수 감소
    @Transactional
    public void decreaseLikeCount(Integer boardId, String username) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("No board found with id: " + boardId));
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No user found with username: " + username));

        LikeEntity like = likeRepository.findByBoardAndUser(board, user)
                .orElseThrow(() -> new IllegalStateException("Not liked the post."));

        likeRepository.delete(like);
        boardRepository.decreaseLikeCount(boardId);
    }

    public boolean hasLiked(Integer boardId, String username) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("No board found with id: " + boardId));
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No user found with username: " + username));

        return likeRepository.findByBoardAndUser(board, user).isPresent();
    }
}
