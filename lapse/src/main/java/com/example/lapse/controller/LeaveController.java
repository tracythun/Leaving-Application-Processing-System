package com.example.lapse.controller;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.lapse.domain.LeaveApplication;
import com.example.lapse.domain.LeaveType;
import com.example.lapse.domain.Staff;
import com.example.lapse.service.LeaveApplicationService;
import com.example.lapse.service.LeaveApplicationServiceImpl;
import com.example.lapse.service.LeaveTypeService;
import com.example.lapse.service.LeaveTypeServiceImpl;
import com.example.lapse.service.StaffService;
import com.example.lapse.service.StaffServiceImpl;
import com.example.lapse.utils.DateUtils;

@Controller
@RequestMapping("/leave")
public class LeaveController {

	@Autowired 
	private LeaveApplicationService lservice;
	
	@Autowired
	public void setLeaveApplicationService (LeaveApplicationServiceImpl lserviceImpl) {
		this.lservice = lserviceImpl;
	}
	
	@Autowired
	private LeaveTypeService ltservice;
	
	@Autowired
	public void setLeaveTypeService(LeaveTypeServiceImpl ltserviceImpl) {
		this.ltservice = ltserviceImpl;
	}
	
	@Autowired
	private StaffService staffservice;

	@Autowired
	public void setStaffService(StaffServiceImpl sserviceImpl) {
		this.staffservice = sserviceImpl;
	}
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(new LeaveValidator());
	}
	@RequestMapping(value = "/list")
	public String list(Model model) {
		model.addAttribute("leaveapplications", lservice.listAllLeaveApplications());
		return "leaveapplications";
	}
	
	@RequestMapping(value = "/add")
	public String addForm(Model model) {
		model.addAttribute("leaveapplication", new LeaveApplication());
		model.addAttribute("leavetypes", ltservice.findAllLeaveTypeNamesExCL());
		return "applyLeave";

	}
	//missing validation part
	@RequestMapping("/submit")
	public String submit(@ModelAttribute("leaveapplication") LeaveApplication application, HttpSession session, Model model) {
//		if (bindingResult.hasErrors()) {
//			model.addAttribute("leaveapplication", application);
//			model.addAttribute("leavetypes", ltservice.findAllLeaveTypeNamesExCL());
//			return "applyLeave";
//		}
			
		Staff currStaff = staffservice.findStafftById((Integer)session.getAttribute("id"));
		LeaveType leaveType = ltservice.findLeaveTypeByLeaveType(application.getLeaveType().getLeaveType());
		application.setStaff(currStaff);
		application.setLeaveType(leaveType);
		Calendar calStart = DateUtils.dateToCalendar(application.getStartDate());
	    Calendar calEnd = DateUtils.dateToCalendar(application.getEndDate());
		float daysBetween = ChronoUnit.DAYS.between(calStart.toInstant(), calEnd.toInstant()) + 1;
		if(daysBetween <= 14) {
			daysBetween = DateUtils.removeWeekends(calStart, calEnd);
		}
		application.setNoOfDays(daysBetween);
		lservice.addLeaveApplication(application);
		
		return "homePage";
	}
	
	 @RequestMapping(value = "/viewallpending")
	 public String viewpendingleaveapproval(Model model,HttpSession session) {	
		 int id=(int) session.getAttribute("id");
		 List<LeaveApplication> PendingLeaveList=lservice.findpendingleaveapproval(id);					  		 			 					
		 model.addAttribute(("LeaveApplication"), PendingLeaveList);
		 return"Managerapproval";
	 }
	 
	 @RequestMapping(value = "/approve/{id}")
	 public String approveleaveapplication(@PathVariable("id") Integer id) {
	 lservice.approveleaveapplication(id);
	 return "Managerapproval";
}

    @RequestMapping(value = "/reject/{id}")
    public String rejectleaveapplication(@PathVariable("id") Integer id) {
    lservice.rejectleaveapplication(id);
    return "Managerapproval";	
    } 
    
    @RequestMapping(value="/viewdetails/{id}")
	public String viewDetailPending(@PathVariable("id") int id,Model model)
	{
		LeaveApplication leave=lservice.findApplicationById(id);
		
		model.addAttribute("leaveapplication", leave);
		return "viewDetailPending";
	}
	
	@RequestMapping(value = "/updateStatus")
	public String updatePendingStatus(@ModelAttribute("leaveapplication") LeaveApplication leaveApp, Model model) {
		lservice.updateLeaveStatus(leaveApp.getId(), leaveApp.getLeaveStatus(), leaveApp.getManagerComment());
		return "forward:/leave/viewallpending";
	}
	
	//delete not tested
	@RequestMapping(value = "/delete/{id}")
	public String deleteLeaveapplication(@PathVariable("id") Integer id) {
		lservice.deleteLeaveApplication(lservice.findApplicationById(id));
		return "forward:/applyleave/viewApprove";
	}
}
