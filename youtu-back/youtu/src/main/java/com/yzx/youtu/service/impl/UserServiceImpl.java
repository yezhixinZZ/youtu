package com.yzx.youtu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzx.youtu.constant.UserConstant;
import com.yzx.youtu.exception.BusinessException;
import com.yzx.youtu.exception.ErrorCode;
import com.yzx.youtu.exception.ThrowUtils;
import com.yzx.youtu.mapper.UserMapper;
import com.yzx.youtu.model.dto.user.UserQueryRequest;
import com.yzx.youtu.model.entity.User;
import com.yzx.youtu.model.vo.LoginUserVO;
import com.yzx.youtu.model.vo.UserVO;
import com.yzx.youtu.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import com.yzx.youtu.model.enums.UserRoleEnum;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Merer
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-12-13 17:29:29
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验数据
        if(StrUtil.hasBlank(userAccount, userPassword, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 16, ErrorCode.PARAMS_ERROR,"用户账号为8-16位字符串");
        ThrowUtils.throwIf(userPassword.length() < 8 || userPassword.length() > 16, ErrorCode.PARAMS_ERROR,"用户密码为8-16位字符串");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR,"两次密码不一致");

        //2.校验账号是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        Long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR,"账号重复");

        //3.加密密码
        String encryptPassword = getEncryptPassword(userPassword);

        //4.插入数据
        final String defaultUserAvatar = "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";
        final String defaultUserProfile = "介绍一下自己吧";
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("user_" + userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setUserProfile(defaultUserProfile);
        user.setUserAvatar(defaultUserAvatar);

        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，系统错误");
        }
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 脱敏用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        //1.校验数据
        if (StrUtil.hasBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 16, ErrorCode.PARAMS_ERROR,"用户账号格式错误");
        ThrowUtils.throwIf(userPassword.length() < 8 || userPassword.length() > 16, ErrorCode.PARAMS_ERROR,"用户密码格式错误");
        //2.对用户传递的密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3.查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        //不存在，返回错误
        if (user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        //4.保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,user);
        return this.getLoginUserVO(user);
    }

    /**
     * 加密密码
     * @param userPassword 密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        //盐值，混淆密码
        final String SALT = "yezx";
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes());
    }

    /**
     * 获取当前登录用户
     * @param request 请求
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //从数据库查询（确保数据准确，会影响部分性能）
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取脱敏用户信息
     * @param user 用户
     * @return 脱敏信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null){
            return null;
        }
        LoginUserVO loginUserVO= new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏用户信息
     * @param user 用户
     * @return 脱敏信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null){
            return null;
        }
        UserVO UserVO= new UserVO();
        BeanUtil.copyProperties(user, UserVO);
        return UserVO;
    }

    /**
     * 获取脱敏用户列表
     * @param userList 用户列表
     * @return 脱敏用户列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return userList.stream().
                map(this::getUserVO)
                .collect(Collectors.toList());
    }

    /**
     * 退出登录
     * @param request 请求
     * @return 退出登录用户
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        //判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"未登录");
        }
        //移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userAccount),"userAccount",userAccount);
        queryWrapper.eq(StrUtil.isNotBlank(userRole),"userRole",userRole);
        queryWrapper.like(StrUtil.isNotBlank(userName),"userName",userName);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}




