package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误,我们会发现，在密码比对错误后会跳转到异常类中，我们传过去的这个值就是为什么错的原因
            //并且我们的异常处理器会捕获异常后在GlobalExceptionHandler类中生成一个对象返回给前台
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定，statusconstant是一个状态常量，其中有两个static值，DISABLE的值为1，表示禁用，0为启用
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 员工注册
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //这里要将数据传到数据库中，建议使用实体类而不是DTO类
        Employee employee=new Employee();

        //对象属性拷贝,使用内置类BeanUtils中的拷贝方法，将employeeDTO中的数据拷贝到employee，注意二者对性的属性名要一致
        //拷贝只会考取相同属性的值，其他的值都是某人的null
        BeanUtils.copyProperties(employeeDTO,employee);

        //添加账号的状态,不过在我们的数据库中某人状态为1，1为启用，0为禁用
        employee.setStatus(StatusConstant.ENABLE);

        //添加账号的密码，密码默认是123456，不过还有进行md5加密后添加到数据库中
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置账号创建的时间和最近修改的时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id，这里暂时先写死
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        //调用接口添加到数据库中
        employeeMapper.insert(employee);
    }
}
