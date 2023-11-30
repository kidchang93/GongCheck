package com.gck.member.service;

import com.gck.encryption.Sha256;
import com.gck.factory.MyBatisFactory;
import com.gck.member.model.MemberDAO;
import com.gck.member.model.PasswordMemberDAO;
import com.gck.member.model.PasswordMemberVO;
import org.apache.ibatis.session.SqlSession;
import java.util.HashMap;


public class MemberService {
    // DAO
    SqlSession sqlSession;
    MemberDAO mapperMembers;
    PasswordMemberDAO mapperPasswordMembers;

    // 생성자
    public MemberService(){}

    // 회원 id로 회원 idx 검색
    // memberId, passwordMember에 맞는 회원의 Idx를 리턴. 없으면 -1 리턴
    public int getMemberIdx(String memberId, String passwordMember){
        Integer memberIdx = null;

        try {
            this.sqlSession = MyBatisFactory.getSqlSession();
            mapperMembers = this.sqlSession.getMapper(MemberDAO.class);

            // 비밀번호 암호화
            String encryptedPasswordMember = Sha256.getHash(passwordMember, memberId);
            System.out.println("encryptedPasswordMember : " + encryptedPasswordMember);
            HashMap<String, String> map = new HashMap<>();
            map.put("memberId", memberId);
            map.put("passwordMember", encryptedPasswordMember);

            // memberId, passwordMember에 맞는 회원이 없으면 null을 리턴받는다.
            memberIdx = mapperMembers.getMemberIdx(map);

            sqlSession.close();
        }catch(Exception e){
            System.out.println("MemberService_exception");
        }finally {
            // memberId, passwordMember에 맞는 회원이 없어서 null을 리턴받으면 -1 리턴
            return (memberIdx != null) ? memberIdx.intValue() : -1;
        }
    }

    // 회원가입
    // 회원가입 성공 시, true 리턴. 실패 시, false 리턴
    public boolean signup(String memberId, String passwordMember, String memberNickname, String memberEmail, String memberZonecode, String memberAddress, String memberAddressDetailed){
        boolean result = false;
        try {
            this.sqlSession = MyBatisFactory.getSqlSession();
            mapperMembers = this.sqlSession.getMapper(MemberDAO.class);
            mapperPasswordMembers = this.sqlSession.getMapper(PasswordMemberDAO.class);

            // 비밀번호 암호화
            String encryptedPasswordMember = Sha256.getHash(passwordMember, memberId);
            System.out.println("encryptedPasswordMember : " + encryptedPasswordMember);

            // map에 회원정보 입력
            HashMap<String, String> map = new HashMap<>();
            map.put("memberId", memberId);
            // map.put("passwordMember", encryptedPasswordMember);
            map.put("memberNickname", memberNickname);
            map.put("memberEmail", memberEmail);
            map.put("memberZonecode", memberZonecode);
            map.put("memberAddress", memberAddress);
            map.put("memberAddressDetailed", memberAddressDetailed);

            // 비밀번호 제외한 회원 정보 insert
            Integer resultInsertMember = mapperMembers.insertMember(map);
            // insert 실패
            if(resultInsertMember == null || resultInsertMember != 1){
                System.out.println("resultInsertMember");
                return result;
            }

            // 회원 idx 조회
            Integer memberIdx = mapperMembers.getMemberIdxById(memberId);
            // 조회 실패
            if(memberIdx == null){
                System.out.println("memberIdx");
                return result;
            }

            // 회원 비밀번호 객체 생성
            PasswordMemberVO passwordMemberVO = new PasswordMemberVO(memberIdx.intValue(), encryptedPasswordMember);

            // 비밀번호 insert
            Integer resultInsertPassword = mapperPasswordMembers.insertPassword(passwordMemberVO);
            // insert 실패
            if(resultInsertPassword == null || resultInsertPassword != 1) {
                System.out.println("resultInsertPassword");
                return result;
            }

            // 회원가입 성공
            result = true;
        }catch (Exception e){
            System.out.println("MemberService_exception_signup");
        }finally {
            sqlSession.close();
            return result;
        }
    }

    // id 중복 확인
    public boolean searchId(String memberId){
        // true 리턴 : 중복 O / false 리턴 : 중복 X
        // boolean exists = getMemberService().searchId(memberId);
        boolean result = true;

        try {
            this.sqlSession = MyBatisFactory.getSqlSession();
            mapperMembers = this.sqlSession.getMapper(MemberDAO.class);

            // id 개수 조회
            Integer exists = mapperMembers.searchId(memberId);

            if(exists == null || exists.intValue() != 0){ // 오류 || 중복 O
                // result = true;
            }else if(exists.intValue() == 0){ // 중복 X
                result = false;
            }
        }catch (Exception e){
            System.out.println("MemberService_exception_searchId");
        }finally {
            sqlSession.close();
            return result;
        }
    }

}
