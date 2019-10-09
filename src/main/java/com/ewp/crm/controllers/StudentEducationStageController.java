package com.ewp.crm.controllers;

import com.ewp.crm.models.Course;
import com.ewp.crm.models.StudentEducationStage;
import com.ewp.crm.service.interfaces.CourseService;
import com.ewp.crm.service.interfaces.StudentEducationStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'OWNEN', 'HR', 'MENTOR')")
@RequestMapping(value = "/studenteducationstage")
public class StudentEducationStageController {

    private final StudentEducationStageService studentEducationStageService;
    private final CourseService courseService;

    @Autowired
    public StudentEducationStageController(StudentEducationStageService studentEducationStageService, CourseService courseService) {
        this.studentEducationStageService = studentEducationStageService;
        this.courseService = courseService;
    }

    @GetMapping(value = "/{courseId}")
    public ModelAndView getAllStudentEducationStageByCourse(@PathVariable("courseId") Long courseId) {
        Course course = courseService.getCourse(courseId);
        ModelAndView modelAndView = new ModelAndView("studenteducationstage");
        modelAndView.addObject("courses", course);
        modelAndView.addObject("student_education_stage", studentEducationStageService.getStudentEducationStageByCourse(course));
        return modelAndView;
    }

    @GetMapping(value = "/add")
    public String addStudentEducationStage(){
       return "/add";
    }

    @PostMapping (value = "/add")
    public String addStudentEducationStage(@RequestParam (name = "studentEducationStageName") String studentEducationStageName,
                                           @RequestParam (name = "studentEducationStageLevel", required = false) Integer studentEducationStageLevel,
                                           @RequestParam (name = "courseId") Long courseId) {
        ModelAndView modelAndView = new ModelAndView("studenteducationstage");
        StudentEducationStage studentEducationStage = new StudentEducationStage();
        if(studentEducationStageLevel!=null) {
            studentEducationStage.setEducationStageLevel(studentEducationStageLevel);
        }
        studentEducationStage.setEducationStageName(studentEducationStageName);
        Course course = courseService.getCourse(courseId);
        studentEducationStageService.add(studentEducationStage, course);
        return "redirect:/courses";
    }

    @RequestMapping(value = "delete/{studentEducationStageId}")
    public String deleteStudentEducationStageBy(@PathVariable("studentEducationStageId") Long studentEducationStageId, Model model) {
        StudentEducationStage studentEducationStage = studentEducationStageService.getStudentEducationStage(studentEducationStageId);
        Long courseId = 0l;
        if(studentEducationStage!=null) {
            studentEducationStageService.deleteCustom(studentEducationStage);
        }
       // model.addAttribute("courseId", courseId);
        return "redirect:/courses";
    }

//    @GetMapping(value = "/update/{id}")
//    public String updateStudentEducationStage(@PathVariable(name = "id") Long id, Model model){
//        StudentEducationStage studentEducationStage = studentEducationStageService.getStudentEducationStage(id);
//        if(studentEducationStage!=null) {
//            model.addAttribute("studentEducationStage", studentEducationStage);
//        }
//        return "redirect:/update";
//    }

    @RequestMapping(value = "/update")
    public String updateStudentEducationStage(@RequestParam(name = "studentEducationStageId") Long id,
                                              @RequestParam (name = "studentEducationStageName") String studentEducationStageName,
                                              @RequestParam (name = "studentEducationStageLevel", required = false) Integer studentEducationStageLevel,
                                              Model model) {
        StudentEducationStage studentEducationStage = new StudentEducationStage();
        studentEducationStage.setId(id);
        studentEducationStage.setEducationStageLevel(studentEducationStageLevel);
        studentEducationStage.setEducationStageName(studentEducationStageName);
    //    Long courseId = studentEducationStage.getCourse().getId();
        studentEducationStageService.update(studentEducationStage);
        return "redirect:/courses";
    }
}
