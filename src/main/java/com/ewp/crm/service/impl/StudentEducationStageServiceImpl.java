package com.ewp.crm.service.impl;

import com.ewp.crm.models.Course;
import com.ewp.crm.models.StudentEducationStage;
import com.ewp.crm.repository.interfaces.StudentEducationStageRepository;
import com.ewp.crm.service.interfaces.StudentEducationStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentEducationStageServiceImpl extends CommonServiceImpl<StudentEducationStage> implements StudentEducationStageService {
    @Autowired
    private StudentEducationStageRepository studentEducationStageRepository;


    @Override
    public void add(StudentEducationStage studentEducationStage, Course course) {
        studentEducationStageRepository.add(studentEducationStage, course);
    }

    @Override
    public void update(StudentEducationStage studentEducationStage, Course course) {
        studentEducationStageRepository.update(studentEducationStage, course);
    }

    @Override
    public StudentEducationStage getStudentEducationStage(Long id) {
        return studentEducationStageRepository.getOne(id);
    }

    @Override
    public List<StudentEducationStage> getStudentEducationStageByCourse(Course course) {
        return studentEducationStageRepository.getStudentEducationStageByCourse(course);
    }
}
