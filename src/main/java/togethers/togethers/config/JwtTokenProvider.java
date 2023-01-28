package togethers.togethers.config;

import groovy.util.logging.Slf4j;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import togethers.togethers.data.dto.UserDetails;
import togethers.togethers.service.UserDetailsService;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final UserDetailsService userDetailsService;

    @Value("${spring.jwt.secret}")
    private String secretKey = "secretKey";
    private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final Long tokenValidMillisecond = 1000L*60*60;
    @PostConstruct
    protected void init(){
        LOGGER.info("[init] JwtTokenProvider 내 secretKey 초기화 시작");
//        sercretKey = Base64.getEncoder();
//            encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        key = Keys.hmacShaKeyFor(keyBytes);

        secretKey = Encoders.BASE64.encode(key.getEncoded());

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));


        LOGGER.info("[init] JwtTokenProvider 내  secretKey 초기화 완료");

    }

    public String createToken(String userId, List<String> roles){
        LOGGER.info("[createToken] 토큰 생성 시작");
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("roles",roles);
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+tokenValidMillisecond))
                .signWith(key)
                .compact();

        LOGGER.info("[createToken] 토큰 생성 완료");
        return token;
    }

    public Authentication getAuthentication(String token){
        LOGGER.info("[getAuthentication] 토큰 인증 정보 조회 시작");
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUsername(token));
        LOGGER.info("[getAuthentication] 토큰 인증 정보 조회 완료, UserDetails UserName : {}", userDetails.getUsername());
        return new UsernamePasswordAuthenticationToken(userDetails,"", userDetails.getAuthorities());
    }

    public String getUsername(String token){
        LOGGER.info("[getUsername] 토큰 기반 회원 구별 정보 추출");
        String info = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
        LOGGER.info("[getUsername] 토큰 기반 회언 구별 정보 추출 완료, info : {}", info);
        return info;
    }

    public String resolveToken(HttpServletRequest request){
        LOGGER.info("[resolveToken] HTTP 헤더에서 Token 값 추출");
        return request.getHeader("X-AUTH-TOKEN");
    }

    public boolean validateToken(String token){
        LOGGER.info("[validateToken] 토큰 유효 체크 시작");
        try{
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        }catch(Exception e){
            LOGGER.info("[validateToken] 토큰 유효 체크 예외 발생");
            return false;
        }
    }
}
