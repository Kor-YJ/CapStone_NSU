package togethers.togethers.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import togethers.togethers.entity.MemberDetail;
import togethers.togethers.entity.Post;
import togethers.togethers.entity.Reply;
import togethers.togethers.form.MemberDetailForm;
import togethers.togethers.form.Postform;
import togethers.togethers.form.replyForm;
import togethers.togethers.service.MemberService;

import javax.validation.Valid;
import java.util.Date;

@Controller
@RequiredArgsConstructor
public class PostController {


    private final MemberService memberService;

    @GetMapping("/write")
    public String postFormCreate(Model model, Postform postform) {
        model.addAttribute("Postform", postform);
        return "write";
    }

    @PostMapping("/write")
    public String PostCreate(@Valid Postform postform) {
        Post post = new Post(postform);
        memberService.post_write(post);
        return "redirect:/";
    }

    @GetMapping("/postlist")
    public String postlist() {
        return "post/postlist";
    }


    @GetMapping("/detailPost")
    public String detailPost() {
        return "post/detailPost";
    }




}
