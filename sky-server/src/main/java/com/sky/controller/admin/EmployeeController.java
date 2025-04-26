package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);
        //登录成功后，生成jwt令牌，jwt令牌使用createjwt方法生成，其中的参数根据传过来的jwtproperties获取
        //jwtproperteis则根据配置文件中的sky.jwt中的数据来获取数据
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);
        //这里是一个新的对象类，这个类中将jwt令牌，用户名，密码，序号封装在一起，是用来builder创建对象
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();
//这里是调用success方法再次封装成一个result对象，我们的result对象是统意传给前端使用的对象，其中在封装时我们会传入code属性，
// 值为1表示封装成功，值为0或其他值表示未封装成功。传入data属性来表示我们的值的类型为object。
// 还有一个方法时error表示封装失败的时候，之后我们会把data属性变成msg，里面的数据为失败的原因
        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工注销")
    public Result<String> logout() {
        return Result.success();
    }

    //这是有关于新员工注册的控制器,路径在方法上面已经加过了

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping
    //添加文档注解
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        //添加日志信息，其中employDTO的信息胡自动输入到大括号中
        log.info("新增员工，{}",employeeDTO);
        employeeService.save(employeeDTO);
        return null;
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    //这里参数不需要加入@RequestBody的原因是因为前端传过来的数据并不是json格式的
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询，{}",employeePageQueryDTO);
         PageResult pageResult=employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     * @return
     */
    //这里我们不需要泛型，对于非查询类，只需要返回code就可以，查询类需要返回数据，这时候在使用泛型
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result starOrStop(@PathVariable Integer status,Long id){
        log.info("启用禁用账号：{}，{}",status,id);
        employeeService.startOrStop(status,id);
        return Result.success();
    }


    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee=employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("修改员工信息，{}",employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success(employeeDTO);
    }
}
