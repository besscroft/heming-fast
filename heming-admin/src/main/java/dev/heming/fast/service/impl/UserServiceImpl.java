package dev.heming.fast.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import dev.heming.fast.constant.CacheConstants;
import dev.heming.fast.constant.SystemConstants;
import dev.heming.fast.converter.UserConverterMapper;
import dev.heming.fast.entity.User;
import dev.heming.fast.exception.HeMingException;
import dev.heming.fast.mapper.UserMapper;
import dev.heming.fast.param.user.PageListParam;
import dev.heming.fast.param.user.UserAddParam;
import dev.heming.fast.param.user.UserUpdateParam;
import dev.heming.fast.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 用户 Service 实现类
 * @Author Bess Croft
 * @Date 2023/5/25 19:50
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public SaTokenInfo login(String username, String password) {
        ServletUriComponentsBuilder request = ServletUriComponentsBuilder.fromCurrentRequest();
        log.info("用户发起登录请求:{}，请求 uri 为：{}", username, request.toUriString());
        User user = this.baseMapper.selectByUsername(username);
        Assert.notNull(user, "账号或密码错误！");
        if (Objects.equals(user.getStatus(), SystemConstants.STATUS_NO)) {
            throw new HeMingException(String.format("账号：%s 已被禁用，请联系管理员！", username));
        }
        if (!Objects.equals(SecureUtil.sha256(password), user.getPassword())) {
            throw new HeMingException("账号或密码错误！");
        }
        // 登录
        StpUtil.login(user.getId());
        this.updateById(user);
        return StpUtil.getTokenInfo();
    }

    @Override
    public Map<String, Object> getUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = getUserById(userId);
        Assert.notNull(user, "暂未登录！");
        Map<String, Object> map = new HashMap<>();
        map.put("userName", user.getName());
        map.put("avatar", user.getAvatar());
        map.put("email", user.getEmail());
        return map;
    }

    @Override
    public User getUser(String username) {
        return this.baseMapper.selectByUsername(username);
    }

    @Override
    @Cacheable(value = CacheConstants.USER, key = "#userId", unless = "#result == null")
    public User getUserById(Long userId) {
        log.info("查询用户信息：{}", userId);
        return this.baseMapper.selectById(userId);
    }

    @Override
    public List<User> pageList(PageListParam param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        return this.baseMapper.selectUserList(param.getUsername(), param.getName(), param.getTelephone(), param.getEmail());
    }

    @Override
    public void addUser(UserAddParam param) {
        User user = UserConverterMapper.INSTANCE.AddParamToUser(param);
        this.baseMapper.insert(user);
    }

    @Override
    public void updateUser(UserUpdateParam param) {
        User user = UserConverterMapper.INSTANCE.UpdateParamToUser(param);
        this.baseMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long userId) {
        this.baseMapper.deleteById(userId);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

}
