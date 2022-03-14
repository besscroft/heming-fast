package com.besscroft.lfs.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.besscroft.lfs.entity.AuthMenu;
import com.besscroft.lfs.entity.AuthUser;
import com.besscroft.lfs.model.MetaVo;
import com.besscroft.lfs.model.RouterVo;
import com.besscroft.lfs.repository.MenuRepository;
import com.besscroft.lfs.service.MenuService;
import com.besscroft.lfs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author Bess Croft
 * @Time 2021/7/8 15:33
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<RouterVo> getMenuList(Long userId) {
        List<AuthMenu> menuList = menuRepository.findAllByUserId(userId);
        List<AuthMenu> menus = getMenus(menuList);
        log.info("menus:{}", menus);
        return getRouter(menus);
    }

    @Override
    public List<AuthMenu> getMenuListById(Long adminId) {
        List<AuthMenu> authMenuList = menuRepository.findAllByUserId(adminId);
        return getMenus(authMenuList);
    }

    @Override
    public Page<AuthMenu> getMenuPageList(Integer pageNum, Integer pageSize, String keyword) {
        return menuRepository.findAll(PageRequest.of(Objects.equals(pageNum, 0) ? 0 : pageNum - 1, pageSize));
    }

    @Override
    public List<AuthMenu> getParentMenu() {
        return menuRepository.findAllByParentId(0L);
    }

    @Override
    public AuthMenu getMenuById(Long id) {
        return menuRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(AuthMenu authMenu) {
        return menuRepository.save(authMenu) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeSwitch(boolean hidden, Long id, Long adminId) {
        if (hidden) {
            return menuRepository.changeSwitch(1, id) > 0;
        }
        return menuRepository.changeSwitch(0, id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delMenu(List<Long> ids) {
        menuRepository.deleteAllByIdInBatch(ids);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMenu(AuthMenu authMenu) {
        authMenu.setCreateTime(LocalDateTime.now());
        return menuRepository.save(authMenu) != null;
    }

    @Override
    public List<Long> getMenuTreeById(Long id) {
        return menuRepository.selectMenuTreeById(id);
    }

    @Override
    public List<AuthMenu> getAllMenuTree() {
        List<AuthMenu> menuList = menuRepository.findAll();
        return getMenus(menuList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenuTree(List<Long> menuIds, Long id) {
        AuthUser currentAdmin = userService.getCurrentAdmin();
        if (ObjectUtil.isNotEmpty(currentAdmin) && Objects.equals(id, currentAdmin.getId())) {
            // 超级管理员，默认拥有所有菜单，不允许更改！
            return false;
        }
        int i = menuRepository.deleteRoleMenuRelation(id);
        if (i > 0) {
            for (Long menuId : menuIds) {
                menuRepository.insertRoleMenuRelation(menuId, id);
            }
        }
        return true;
    }

    /**
     * 菜单层级处理
     * @param menuList
     * @return
     */
    private List<AuthMenu> getMenus(List<AuthMenu> menuList) {
        List<AuthMenu> parentMenus = menuList.stream().filter(menu -> menu.getParentId() == 0).collect(Collectors.toList());
        List<AuthMenu> menus = menuList.stream().filter(menu -> menu.getParentId() != 0).collect(Collectors.toList());
        parentMenus.forEach(menu -> {
            List<AuthMenu> childMenu = getChildMenu(menu.getId(), menus);
            menu.setChildren(childMenu);
        });
        return parentMenus;
    }

    /**
     * 菜单递归
     * @param menuId 菜单id
     * @param menuList 子菜单集合
     * @return
     */
    private List<AuthMenu> getChildMenu(Long menuId, List<AuthMenu> menuList) {
        List<AuthMenu> menus = menuList.stream().filter(menu -> menu.getParentId() == menuId).collect(Collectors.toList());
        menus.forEach(menu -> {
            List<AuthMenu> childMenu = getChildMenu(menu.getId(), menuList);
            menu.setChildren(childMenu);
        });
        return menus;
    }

    /**
     * 获取路由信息
     * @param menuList 菜单集合
     * @return
     */
    private List<RouterVo> getRouter(List<AuthMenu> menuList) {
        List<RouterVo> routerVoList = new LinkedList<>();
        menuList.forEach(menuDto -> {
            RouterVo routerVo = new RouterVo();
            routerVo.setName(menuDto.getName());
            routerVo.setPath(menuDto.getPath());
            routerVo.setHidden(menuDto.getHidden() != 0);
            routerVo.setComponent(menuDto.getComponent());
            routerVo.setMeta(new MetaVo(menuDto.getTitle(), menuDto.getIcon(), false));
            if (menuDto.getChildren().size() > 0 && !menuDto.getChildren().isEmpty()) {
                routerVo.setAlwaysShow(true);
                routerVo.setRedirect("noRedirect");
                List<RouterVo> childRouter = getChildRouter(menuDto.getChildren());
                routerVo.setChildren(childRouter);
            }
            routerVoList.add(routerVo);
        });
        return routerVoList;
    }

    /**
     * 子路由处理
     * @param menuList 子菜单集合
     * @return
     */
    private List<RouterVo> getChildRouter(List<AuthMenu> menuList) {
        List<RouterVo> list = new ArrayList<>();
        menuList.forEach(child -> {
            RouterVo router = new RouterVo();
            router.setPath(child.getPath());
            router.setName(child.getName());
            router.setComponent(child.getComponent());
            router.setMeta(new MetaVo(child.getTitle(), child.getIcon(), false));
            router.setHidden(child.getHidden() != 0);
            if (child.getChildren().size() > 0 && !child.getChildren().isEmpty()) {
                List<RouterVo> childRouter = getChildRouter(child.getChildren());
                router.setChildren(childRouter);
            }
            list.add(router);
        });
        return list;
    }

}