package dev.heming.fast.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import dev.heming.fast.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description Sa-Token 自定义权限验证接口扩展
 * @Author Bess Croft
 * @Date 2023/1/15 19:56
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final RoleService roleService;

    /**
     * 返回一个账号所拥有的权限码集合
     * @param loginId 登录用户 id
     * @param loginType 登录用户类型
     * @return 账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本项目暂时不需要，所以返回空集合
        return CollUtil.newArrayList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     * @param loginId 登录用户 id
     * @param loginType 登录用户类型
     * @return 账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // TODO 配置缓存
        return roleService.getAllRoleCodeByCurrentUser(Long.parseLong(loginId.toString()));
    }

}
