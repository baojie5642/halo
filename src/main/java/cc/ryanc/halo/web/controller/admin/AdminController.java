package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.enums.*;
import cc.ryanc.halo.model.enums.ResponseStatus;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * <pre>
 *     后台首页控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/5
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin")
public class AdminController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogsService logsService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 请求后台页面
     *
     * @param model model
     * @param
     * @return 模板路径admin/admin_index
     */
    @GetMapping(value = {"", "/index"})
    public String index(Model model) {
        if (null == model) {
            throw new NullPointerException();
        } else {
            commentCount(model);
            postsLatest(model);
            logsLatest(model);
            comments(model);
            mediaCount(model);
            postViewsSum(model);
            hadDays(model);
        }
        return "admin/admin_index";
    }

    private final void commentCount(Model model) {
        //查询评论的条数
        Long commentCount = commentService.getCount();
        model.addAttribute("commentCount", commentCount);
    }

    private final void postsLatest(Model model) {
        //查询最新的文章
        List<Post> postsLatest = postService.findPostLatest();
        model.addAttribute("postTopFive", postsLatest);
    }

    private final void logsLatest(Model model) {
        //查询最新的日志
        List<Logs> logsLatest = logsService.findLogsLatest();
        model.addAttribute("logs", logsLatest);
    }

    private final void comments(Model model) {
        //查询最新的评论
        List<Comment> comments = commentService.findCommentsLatest();
        model.addAttribute("comments", comments);
    }

    private final void mediaCount(Model model) {
        //附件数量
        model.addAttribute("mediaCount", attachmentService.getCount());
    }

    private final void postViewsSum(Model model) {
        //文章阅读总数
        Long postViewsSum = postService.getPostViews();
        model.addAttribute("postViewsSum", postViewsSum);
    }

    private final void hadDays(Model model) {
        //成立天数
        Date blogStart = DateUtil.parse(HaloConst.OPTIONS.get(BlogProperties.BLOG_START.getProp()));
        long hadDays = DateUtil.between(blogStart, DateUtil.date(), DateUnit.DAY);
        model.addAttribute("hadDays", hadDays);
    }

    /**
     * 处理跳转到登录页的请求
     *
     * @param session session
     * @return 模板路径admin/admin_login
     */
    @GetMapping(value = "/login")
    public String login(HttpSession session) {
        User user = (User) session.getAttribute(HaloConst.USER_SESSION_KEY);
        //如果session存在，跳转到后台首页
        if (null != user) {
            return "redirect:/admin";
        }
        return "admin/admin_login";
    }

    /**
     * 验证登录信息
     *
     * @param loginName 登录名：邮箱／用户名
     * @param loginPwd  loginPwd 密码
     * @param session   session session
     * @return JsonResult JsonResult
     */
    @PostMapping(value = "/getLogin")
    @ResponseBody
    public JsonResult getLogin(@ModelAttribute("loginName") String loginName,
            @ModelAttribute("loginPwd") String loginPwd,
            HttpSession session) {
        //已注册账号，单用户，只有一个
        User aUser = userService.findUser();
        if (null == aUser) {
            return new JsonResult(ResultCode.FAIL.getCode(),
                    localeMessageUtil.getMessage("code.admin.user.notexist"));
        }
        if (forbiden(aUser)) {
            return new JsonResult(ResultCode.FAIL.getCode(),
                    localeMessageUtil.getMessage("code.admin.login.disabled"));
        }
        //验证用户名和密码
        User user = null;
        if (Validator.isEmail(loginName)) {
            user = userService.userLoginByEmail(loginName, SecureUtil.md5(loginPwd));
        } else {
            user = userService.userLoginByName(loginName, SecureUtil.md5(loginPwd));
        }
        userService.updateUserLoginLast(DateUtil.date());
        //判断User对象是否相等
        if (ObjectUtil.equal(aUser, user)) {
            session.setAttribute(HaloConst.USER_SESSION_KEY, aUser);
            //重置用户的登录状态为正常
            userService.updateUserNormal();
            logsService.save(LogsRecord.LOGIN, LogsRecord.LOGIN_SUCCESS, request);
            log.info("User {} login succeeded.", aUser.getUserDisplayName());
            return new JsonResult(ResultCode.SUCCESS.getCode(),
                    localeMessageUtil.getMessage("code.admin.login.success"));
        } else {
            //更新失败次数
            Integer errorCount = userService.updateUserLoginError();
            //超过五次禁用账户
            if (errorCount >= CommonParams.FIVE.getValue()) {
                userService.updateUserLoginEnable(TrueFalse.FALSE.getDesc());
            }
            logsService.save(LogsRecord.LOGIN,
                    LogsRecord.LOGIN_ERROR + "[" + HtmlUtil.escape(loginName) + "," + HtmlUtil.escape(loginPwd) + "]",
                    request);
            Object[] args = {(5 - errorCount)};
            return new JsonResult(ResultCode.FAIL.getCode(),
                    localeMessageUtil.getMessage("code.admin.login.failed", args));
        }
    }

    private boolean forbiden(User user) {
        Date last = lastLogin(user);
        long between = between(last);
        // 只有下面两个条件都不成立时才被禁止
        //首先判断是否已经被禁用已经是否已经过了10分钟
        if (StrUtil.equals(user.getLoginEnable(), TrueFalse.TRUE.getDesc())) {
            return false;
        } else if (between >= CommonParams.TEN.getValue()) {
            return false;
        } else {
            return true;
        }
    }

    private Date lastLogin(User aUser) {
        Date last = aUser.getLoginLast();
        if (null != last) {
            return last;
        } else {
            return DateUtil.date();
        }
    }

    private long between(Date last) {
        return DateUtil.between(last, DateUtil.date(), DateUnit.MINUTE);
    }


    /**
     * 退出登录 销毁session
     *
     * @param session session
     * @return 重定向到/admin/login
     */
    @GetMapping(value = "/logOut")
    public String logOut(HttpSession session) {
        User user = (User) session.getAttribute(HaloConst.USER_SESSION_KEY);
        session.removeAttribute(HaloConst.USER_SESSION_KEY);
        logsService.save(LogsRecord.LOGOUT, user.getUserName(), request);
        log.info("User {} has logged out", user.getUserName());
        return "redirect:/admin/login";
    }

    /**
     * 查看所有日志
     *
     * @param model model model
     * @param page  page 当前页码
     * @param size  size 每页条数
     * @return 模板路径admin/widget/_logs-all
     */
    @GetMapping(value = "/logs")
    public String logs(Model model,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "logId");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Logs> logs = logsService.findAll(pageable);
        model.addAttribute("logs", logs);
        return "admin/widget/_logs-all";
    }

    /**
     * 清除所有日志
     *
     * @return 重定向到/admin
     */
    @GetMapping(value = "/logs/clear")
    public String logsClear() {
        try {
            logsService.removeAll();
        } catch (Exception e) {
            log.error("Clear log failed:{}" + e.getMessage());
        }
        return "redirect:/admin";
    }

    /**
     * Halo关于页面
     *
     * @return 模板路径admin/admin_halo
     */
    @GetMapping(value = "/halo")
    public String halo() {
        return "admin/admin_halo";
    }

    /**
     * 获取一个Token
     *
     * @return JsonResult
     */
    @GetMapping(value = "/getToken")
    @ResponseBody
    public JsonResult getToken() {
        String token = (System.currentTimeMillis() + new Random().nextInt(999999999)) + "";
        return new JsonResult(ResultCode.SUCCESS.getCode(), ResponseStatus.SUCCESS.getMsg(),
                SecureUtil.md5(token));
    }
}
