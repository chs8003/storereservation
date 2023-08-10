package com.hyeonsik.boot.service;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hyeonsik.boot.mapper.EmailService;
import com.hyeonsik.boot.mapper.UserMapper;
import com.hyeonsik.boot.vo.UserVo;


@Service
public class RegisterMail implements EmailService {

	@Autowired
	JavaMailSender emailsender; // Bean 등록해둔 MailConfig 를 emailsender 라는 이름으로 autowired

	private String ePw; // 인증번호
	private UserMapper userMapper;
	
	@Autowired
	SqlSession sqlsession;

	// 메일 내용 작성
	@Override
	public MimeMessage createMessage(String to) throws MessagingException, UnsupportedEncodingException {
//		System.out.println("보내는 대상 : " + to);
//		System.out.println("인증 번호 : " + ePw);
		
		MimeMessage message = emailsender.createMimeMessage();

		message.addRecipients(RecipientType.TO, to);// 보내는 대상
		message.setSubject("푸드잇 회원가입 인증");// 제목

		String msgg = "";
		msgg += "<div style='margin:100px;'>";
		msgg += "<h1> 안녕하세요</h1>";
		msgg += "<h1> 푸드잇 입니다.</h1>";
		msgg += "<br>";
		msgg += "<p>아래 코드를 회원가입 창에서 입력해주세요<p>";
		msgg += "<br>";
		msgg += "<p>당신을 위한 슬기로운 외식 생활 - 푸드잇<p>";
		msgg += "<br>";
		msgg += "<div align='center' style='border:1px solid black; font-family:verdana';>";
		msgg += "<h3 style='color:blue;'>회원가입 인증 코드</h3>";
		msgg += "<div style='font-size:130%'>";
		msgg += "CODE : <strong>";
		msgg += ePw + "</strong><div><br/> "; // 메일에 인증번호 넣기
		msgg += "</div>";
		message.setText(msgg, "utf-8", "html");// 내용, charset 타입, subtype
		// 보내는 사람의 이메일 주소, 보내는 사람 이름
		message.setFrom(new InternetAddress("74dydgur@daum.net", "푸드잇 - 회원가입"));// 보내는 사람

		return message;
	}

	// 랜덤 인증 코드 전송
	@Override
	public String createKey() {
		StringBuffer key = new StringBuffer();
		Random rnd = new Random();

		for (int i = 0; i < 8; i++) { // 인증코드 8자리
			int index = rnd.nextInt(3); // 0~2 까지 랜덤, rnd 값에 따라서 아래 switch 문이 실행됨

			switch (index) {
			case 0:
				key.append((char) ((int) (rnd.nextInt(26)) + 97));
				// a~z (ex. 1+97=98 => (char)98 = 'b')
				break;
			case 1:
				key.append((char) ((int) (rnd.nextInt(26)) + 65));
				// A~Z
				break;
			case 2:
				key.append((rnd.nextInt(10)));
				// 0~9
				break;
			}
		}

		return key.toString();
	}

	// 메일 발송
	// sendSimpleMessage 의 매개변수로 들어온 to 는 곧 이메일 주소가 되고,
	// MimeMessage 객체 안에 내가 전송할 메일의 내용을 담는다.
	// 그리고 bean 으로 등록해둔 javaMail 객체를 사용해서 이메일 send!!
	@Override
	public String sendSimpleMessage(String to) throws Exception {

		ePw = createKey(); // 랜덤 인증번호 생성

		// TODO Auto-generated method stub
		MimeMessage message = createMessage(to); // 메일 발송
		try {// 예외처리
			emailsender.send(message);
		} catch (MailException es) {
			es.printStackTrace();
			throw new IllegalArgumentException();
		}


		return ePw; // 메일로 보냈던 인증 코드를 서버로 반환
	}
	
	//아이디 찾기
	 public String get_searchId(String userName, String userEmail) {

			userMapper  = sqlsession.getMapper(UserMapper.class);
			
			String result = "";

			try {
				result = userMapper.searchId(userName, userEmail);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}
	 

	 
	 @Override
		public MimeMessage createMessage2(String userId, String userEmail, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
//			
			MimeMessage message = emailsender.createMimeMessage();

			message.addRecipients(RecipientType.TO, userEmail);// 보내는 대상
			message.setSubject("푸드잇 임시비밀번호");// 제목

			String msgg = "";
			msgg += "<div style='margin:100px;'>";
			msgg += "<h1> 안녕하세요</h1>";
			msgg += "<h1> 푸드잇 입니다.</h1>";
			msgg += "<br>";
			msgg += "<p>임시 비밀번호 로그인 후 반드시 비밀번호를 변경해주세요!<p>";
			msgg += "<br>";
			msgg += "<p>감사합니다!<p>";
			msgg += "<br>";
			msgg += "<div align='center' style='border:1px solid black; font-family:verdana';>";
			msgg += "<h3 style='color:blue;'>임시 비밀번호 코드입니다.</h3>";
			msgg += "<div style='font-size:130%'>";
			msgg += "PASSWORD : <strong>";
			msgg += ePw + "</strong><div><br/> "; // 메일에 인증번호 넣기
			msgg += "</div>";
			message.setText(msgg, "utf-8", "html");// 내용, charset 타입, subtype
			// 보내는 사람의 이메일 주소, 보내는 사람 이름
			message.setFrom(new InternetAddress("74dydgur@daum.net", "푸드잇 - 비멀번호 찾기"));// 보내는 사람

			return message;
		}
	 
					
			public String mailSendWithPassword(String userId, String userEmail, HttpServletRequest request) throws Exception {

				userMapper = sqlsession.getMapper(UserMapper.class);
		

				ePw = createKey(); // 랜덤 인증번호 생성

				// TODO Auto-generated method stub
				MimeMessage message = createMessage2(userId,userEmail,request); // 메일 발송
				try {// 예외처리
					emailsender.send(message);
				} catch (MailException es) {
					es.printStackTrace();
					throw new IllegalArgumentException();
				}
				

		        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		        ePw = passwordEncoder.encode(ePw);
		        
				userMapper.searchPassword(userId, userEmail, ePw);

				return ePw; // 메일로 보냈던 인증 코드를 서버로 반환
			}
			// 비밀번호 암호화해주는 메서드
			// 데이터 베이스 값은 암호한 값으로 저장시킨다.
	
}