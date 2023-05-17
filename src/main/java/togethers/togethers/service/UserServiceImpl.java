package togethers.togethers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import togethers.togethers.config.JwtTokenProvider;
import togethers.togethers.dto.login.*;
import togethers.togethers.dto.mypage.UserDetailSaveDto;
import togethers.togethers.dto.mypage.UserDetailUpdateDto;
import togethers.togethers.entity.Mbti;
import togethers.togethers.entity.Post;
import togethers.togethers.entity.User;
import togethers.togethers.entity.UserDetail;
import togethers.togethers.repository.MbtiRepository;
import togethers.togethers.repository.PostRepository;
import togethers.togethers.repository.UserDetailRepository;
import togethers.togethers.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PostRepository postRepository;
    @Autowired
    private final UserDetailRepository userDetailRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MbtiRepository mbtiRepository;


    @Override
    @Transactional
    public String join(User user) {
        userRepository.save(user);
        return user.getUid();
    }

    @Override
    @Transactional
    public UserDetail findUserDetailByUserDetailId(Long userDetailId)
    {
        return userDetailRepository.findByUserDetailId(userDetailId).orElse(null);
    }

    @Override
    @Transactional
    public User findUserByEmail(String email)
    {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    @Transactional
    public User findPostByPostId(Long postId)
    {
        return userRepository.findByPost_PostId(postId).orElse(null);
    }


    @Override
    @Transactional(readOnly = false)
    public void saveIntro(String uid, UserDetailSaveDto userDetailSaveDto) {

        log.info("[saveIntro] 유저 세부사항 저장 Service 로직 동작. userid :{}",uid);
        User user = userRepository.findByUid(uid).orElse(null);

        UserDetail userDetail = new UserDetail(userDetailSaveDto);
        userDetail.setUser(user);
        userDetailRepository.save(userDetail);

        user.setUserDetail(userDetail);
        userRepository.flush();

    }


    @Override
    @Transactional(readOnly = false)
    public void editIntro(Long userDetailId, UserDetailSaveDto userDetailSaveDto) {
        UserDetail userDetail = userDetailRepository.findByUserDetailId(userDetailId).orElse(null);
        UserDetail tempDetail = new UserDetail(userDetailSaveDto);

        userDetail = tempDetail;
        userDetailRepository.flush();
    }


    @Override
    @Transactional
    public User findUserByIdAndPhoneNum(FindIdPhoneDto findIdPhoneDto)
    {
        log.info("[findId] 아이디 핸드폰 번호로 Service 로직 동작. Name : {} ,  phoneNum : {}", findIdPhoneDto.getName(), findIdPhoneDto.getPhoneNum());
        User user = userRepository.findByNameAndPhoneNum(findIdPhoneDto.getName(), findIdPhoneDto.getPhoneNum()).orElse(null);
        return user;
    }

    @Override
    @Transactional
    public User findUserByEmail(FindIdEmailDto findIdEmailDto)
    {
        log.info("[findIdByEmail] 아이디 이메일로 찾기 Service 로직 동작. name: {}, email : {}",findIdEmailDto.getName(),findIdEmailDto.getEmail());
        User user = userRepository.findByNameAndEmail(findIdEmailDto.getName(), findIdEmailDto.getEmail()).orElse(null);
        return user;
    }

    @Override
    @Transactional
    public User findUserByUserDetailId(Long userDetailId)
    {
        return userRepository.findByUserDetail_UserDetailId(userDetailId).orElse(null);
    }

    @Override
    @Transactional
    public MailDto sendEmail(FindPasswordDto findPasswordDto){

        log.info("[sendEmail] 임시 비밀번호 발급 Service 로직 동작. name :{}, email :{}, id:{}",findPasswordDto.getName(),findPasswordDto.getEmail(),findPasswordDto.getId());
        User user = userRepository.findByNameAndEmailAndUid(findPasswordDto.getName(), findPasswordDto.getEmail(), findPasswordDto.getId()).orElse(null);
        if(user == null)
        {
            return null;
        }else {
            String tempPassword = getTempPassword();
            MailDto mailDto = new MailDto();
            mailDto.setAddress(findPasswordDto.getEmail());
            mailDto.setTitle("Together 임시비밀번호 안내 이메일 입니다.");
            mailDto.setMessage("안녕하세요. Together 임시비밀번호 안내 관련 이메일입니다.\n"+" 회원님의 임시 비밀번호는 \n"+tempPassword+"입니다. \n로그인 후에 비밀번호를 변경을 해주세요.");

            user.setPassword(passwordEncoder.encode(tempPassword));
            userRepository.flush();

            return mailDto;
        }
    }


    @Override
    @Transactional
    public String getTempPassword(){
        char[] charSet = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

        String str ="";

        int idx =0;
        for(int i =0; i<10;i++){
            idx = (int)(charSet.length*Math.random());
            str += charSet[idx];
        }
        return str;
    }

    @Override
    @Transactional
    public void mailSend(MailDto mailDto){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailDto.getAddress());
        message.setSubject(mailDto.getTitle());
        message.setText(mailDto.getMessage());
        message.setFrom("wjdghrbs0504@gmail.com");
        message.setReplyTo("wjdghrbs0504@gmail.com");
        System.out.println("message"+message);
        mailSender.send(message);
    }

    @Override
    @Transactional
    public boolean updatePassword(PasswordUpdatedDto passwordUpdatedDto) {

        User user = userRepository.findByUid(passwordUpdatedDto.getUid()).orElse(null);

        if (!passwordEncoder.matches(passwordUpdatedDto.getOriginPassword(), user.getPassword())) {
            log.info("[updatePassword] 기존 비밀번호와 사용자가 입력한 비멀번호 불일치");
            return false;
        } else {
            log.info("[updatePassword] 기존 비밀번호와 사용자가 입력한 비멀번호 일치함 비밀번호 변경 로직 동작");
            user.setPassword(passwordEncoder.encode(passwordUpdatedDto.getNewPassword()));
            userRepository.flush();
            return true;
        }
    }
    @Override
    @Transactional
    public List<Post> matching(String uid) {
        User user = userRepository.findByUid(uid).orElse(null);
        UserDetail ud = userDetailRepository.findById(user.getUserDetail().getUserDetailId()).orElse(null);
        List<UserDetail> sameGenderList = userDetailRepository.findAllByGender(ud.getGender()); // 성별 필터

        List<UserDetail> sameMbtiList = new ArrayList<>();
        Mbti userMbti = mbtiRepository.findByMbti(ud.getMbti()).orElse(null);

        log.info("[matching] 매칭 알고리즘 시작. userID:{}, userMbti :{}",uid,ud.getMbti());



        for (UserDetail i : sameGenderList)
        {
            log.info(i.getMbti());
            if (i.getMbti().equals(userMbti.getFirstMbti()) || i.getMbti().equals(userMbti.getSecondMbti()) || i.getMbti().equals(userMbti.getThirdMbti()) || i.getMbti().equals(userMbti.getFourthMbti()))
            {
                sameMbtiList.add(i);
            }
            else
            {
                continue;
            }
        }

        List<Post> recommendPost = new ArrayList<>();
        for (UserDetail x : sameMbtiList) {
            User temp_user = userRepository.findByUserDetail_UserDetailId(x.getUserDetailId()).orElse(null);
            if(temp_user == null ||temp_user.getPost()==null)
            {
                continue;
            }else{
                Post post = postRepository.findBypostId(temp_user.getPost().getPostId()).orElse(null);
                recommendPost.add(post);
            }
        }

        if (recommendPost.size() < 4)
        {
            log.info("[matching] recommend_post의 갯수가 4개이하여서 추가 등록 로직 동작");
            for (UserDetail userDetail : sameGenderList) {
                User sameGenderUser = userRepository.findByUserDetail_UserDetailId(userDetail.getUserDetailId()).orElse(null);
                log.info("[matching] same_gender_user id :{}",sameGenderUser);

                if(sameGenderUser == null || sameGenderUser.getPost() == null || user.getId() == sameGenderUser.getId())
                {
                    continue;
                }else {
                    Post post = postRepository.findBypostId(sameGenderUser.getPost().getPostId()).orElse(null);
                    log.info("[matching] 추천 게시물에 추가할 게시물 ok : {}",post.getPostId());
                    recommendPost.add(post);
                }
                if (recommendPost.size() == 4)
                {
                    break;
                }
            }
        }

        log.info("[matching] recomment_post 게시물 갯수 :{}",recommendPost.size());
        return recommendPost;
    }

}