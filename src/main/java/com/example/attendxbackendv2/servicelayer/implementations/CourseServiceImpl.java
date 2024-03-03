package com.example.attendxbackendv2.servicelayer.implementations;

import com.example.attendxbackendv2.datalayer.entities.CourseEntity;
import com.example.attendxbackendv2.datalayer.entities.DepartmentEntity;
import com.example.attendxbackendv2.datalayer.entities.LecturerEntity;
import com.example.attendxbackendv2.datalayer.repositories.CourseRepository;
import com.example.attendxbackendv2.datalayer.repositories.DepartmentRepository;
import com.example.attendxbackendv2.datalayer.repositories.LecturerRepository;
import com.example.attendxbackendv2.presentationlayer.datatransferobjects.CourseDTO;
import com.example.attendxbackendv2.servicelayer.exceptions.CourseAlreadyExistsException;
import com.example.attendxbackendv2.servicelayer.exceptions.ResourceNotFoundException;
import com.example.attendxbackendv2.servicelayer.interfaces.CourseService;
import com.example.attendxbackendv2.servicelayer.mappers.CourseMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Value("${pagination.size}")
    private int pageSize;
    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public CourseServiceImpl(LecturerRepository lecturerRepository, DepartmentRepository departmentRepository, CourseRepository courseRepository) {
        this.lecturerRepository = lecturerRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
    }



    @Override
    @Transactional
    public void createCourse(CourseDTO courseDTO) throws ResourceNotFoundException, CourseAlreadyExistsException {
        LecturerEntity lecturer= lecturerRepository.findLecturerEntityByEmailIgnoreCase(courseDTO.getLecturerEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "email", courseDTO.getLecturerEmail()));
        DepartmentEntity department = departmentRepository.findByDepartmentNameIgnoreCase(courseDTO.getDepartmentName())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "name", courseDTO.getDepartmentName()));
        CourseEntity courseEntity = CourseMapper.mapToCourseEntity(new CourseEntity(), courseDTO);
        courseEntity.setDepartment(department);
        department.addCourse(courseEntity);
        lecturer.addCourse(courseEntity);
        courseEntity.setLecturer(lecturer);
        courseRepository.save(courseEntity);
        lecturerRepository.save(lecturer);
        departmentRepository.save(department);
    }

    @Override
    @Transactional
    public List<CourseDTO> getAllCourses(int pageNo, boolean ascending) {
        Pageable pageable;
        if (ascending) {
            pageable = PageRequest.of(pageNo, pageSize, Sort.by("courseCode").ascending());
        } else {
            pageable = PageRequest.of(pageNo, pageSize, Sort.by("courseCode").descending());
        }
        List<CourseEntity> courseEntities = courseRepository.findAll(pageable).getContent();
        return courseEntities.stream().map(courseEntity -> CourseMapper.mapToCourseDTO(new CourseDTO(), courseEntity, false)).toList();

    }
}
