package com.woori.studylogin.Util;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

//주인이 없는 객체(공용으로 사용할 클래스)
//작업 기능을 참고해서 클래스명과 메소드명을 생성
@Component
public class PaginationUtil {
    //<(처음)<<(이전) 123(현재)45>>(다음)>(마지막)
    //데이터베이스에서 제공하는 페이지정보를 이용해서 페이지번튼에 필요한 값을 생성
    //데이터베이스 페이지번호 0,1,2,3,4.... 화면에 페이지번호 1,2,3,4,5...
    public static Map<String, Integer> Pagination(Page<?> page) {
        int currentPage = page.getNumber()+1; //현재위치한 페이지번호
        int totalPage = page.getTotalPages(); //전체 페이지수
        int blockLimit = 10; //화면에 출력할 페이지번호 개수

        //배열은 하나의 변수에 여러개의 값을 저장
        //map을 이용하면 변수명과 값을 동시에 배열에 저장
        Map<String, Integer> pageInfo = new HashMap<>();
        if (totalPage == 0) {
            totalPage = 1;
        }
        //실질적인 페이지 정보를 계산   10-10 = 0/2=0
        int startPage = Math.max(1, currentPage- blockLimit/2);
        //1~10, 전체 5개 1~5
        int endPage = Math.min(startPage+blockLimit-1, totalPage);
        //이전번호 현재(5)->4번, currentPage-10 : 이전페이지
        int prevPage = Math.max(1, currentPage-1);
        //다음번호
        int nextPage = Math.min(totalPage, currentPage+1);
        //마지막페이지번호
        int lastPage = totalPage;

        //계산한 정보를 저장
        pageInfo.put("startPage", startPage);  //1번
        pageInfo.put("endPage", endPage);     //화면에 출력되는 끝페이지번호 10번
        pageInfo.put("prevPage", prevPage);   //현재번호-1
        pageInfo.put("currentPage", currentPage);  //현재번호
        pageInfo.put("nextPage", nextPage);  //현재번호+1
        pageInfo.put("lastPage", lastPage);   //마지막 번호

        return pageInfo;
    }
}
