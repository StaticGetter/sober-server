package com.atschx.adnetwork.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atschx.adnetwork.domain.model.User;
import com.atschx.adnetwork.domain.repository.UserRepository;
import com.atschx.adnetwork.protocol.Result;

@RestController
public class UserController extends AdNetworkController {

	private final UserRepository userRepository;

	@Autowired
	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * 用户列表
	 * @param pageable
	 * @param role
	 * @return
	 */
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public Page<User> users(
			@PageableDefault(value = 15, sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable,
			@RequestParam(value = "role", defaultValue = "") String role) {

		if ("".equals(role)) {
			return userRepository.findAll(pageable);
		}

		return userRepository.findByRoleCode(role, pageable);
	}
	
	/**
	 * 修改密码
	 * 
	 * @param uid
	 * @param oldPassword
	 * @param newPassword
	 * @return
	 */
	@RequestMapping(value = "/change-password", method = RequestMethod.POST)
	Result changePassword(
			@RequestParam Long uid,
			@RequestParam(name="_old") String oldPassword,
			@RequestParam(name="_new") String newPassword){
		
		Result ret = new Result();
		User user = userRepository.findOne(uid);
		
		if (user != null) {
			if(!user.getPassword().equals(oldPassword)){
				ret.setRet("1");// 密码不对
			}else{
				if(oldPassword.equals(newPassword)){
					ret.setRet("2");// 新密码不能和旧密码一致
				}else{
					try {
						//update 
						user.setPassword(newPassword);
						userRepository.saveAndFlush(user);
					} catch (Exception e) {
						ret.setRet("500");
					}
				}
			}
		}else{
			ret.setRet("-1");// 不存在此用户
		}
		
		return ret;
	}
	
	/**
	 * 获取用户资料
	 */
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	User userDetial(@RequestParam Long uid){
		return userRepository.findOne(uid);
	}
	
	/**
	 * 更新用户资料
	 */
	@RequestMapping(value = "/user", method = { RequestMethod.POST }, consumes = { "application/json; charset=UTF-8" })
	Result updateUser(@RequestParam Long uid, @RequestBody User _new) {

		User oldUser = userRepository.findOne(uid);
		if (oldUser != null) {
			oldUser.setName(_new.getName());
			oldUser.setQq(_new.getQq());
			oldUser.setMobile(_new.getMobile());
			userRepository.save(oldUser);
			return new Result();
		}

		return new Result("1");
	}
}
