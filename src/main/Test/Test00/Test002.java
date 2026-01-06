package Test00;

import com.hkd.pojo.AttendanceRecord;
import com.hkd.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
import java.util.List;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/26 12:43
 * @description TODO: 类功能描述
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring.xml","classpath:springmvc.xml"})
public class Test002 {
    @Autowired
    SysUserRoleQueryService sysUserRoleQueryService;
    @Autowired
    AttendanceRecordService attendanceRecordService;
    @Autowired
    EmpInfoService empInfoService;
    @Autowired
    LeaveApplicationService leaveApplicationService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysDeptService sysDeptService;


    //测试前台月考勤数据
    @Test
    public void test10() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        System.out.println(now);
        System.out.println(monthStart);
        List<AttendanceRecord> monthRecords = attendanceRecordService.findByEmpIdAndDateRange(
                1, monthStart, now, -1, 1);
        System.out.println("数据："+monthRecords.size());
    }

}
