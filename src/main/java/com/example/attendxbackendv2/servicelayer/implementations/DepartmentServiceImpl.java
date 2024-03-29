package com.example.attendxbackendv2.servicelayer.implementations;

import com.example.attendxbackendv2.datalayer.entities.DepartmentEntity;
import com.example.attendxbackendv2.datalayer.repositories.DepartmentRepository;
import com.example.attendxbackendv2.presentationlayer.datatransferobjects.AddressDTO;
import com.example.attendxbackendv2.presentationlayer.datatransferobjects.CourseDTO;
import com.example.attendxbackendv2.presentationlayer.datatransferobjects.DepartmentDTO;
import com.example.attendxbackendv2.presentationlayer.datatransferobjects.LecturerDTO;
import com.example.attendxbackendv2.servicelayer.exceptions.DepartmentAlreadyExistsException;
import com.example.attendxbackendv2.servicelayer.exceptions.ResourceNotFoundException;
import com.example.attendxbackendv2.servicelayer.interfaces.DepartmentService;
import com.example.attendxbackendv2.servicelayer.mappers.CourseMapper;
import com.example.attendxbackendv2.servicelayer.mappers.DepartmentMapper;
import com.example.attendxbackendv2.servicelayer.mappers.LecturerMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Value("${pagination.size}")
    private int pageSize;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public void createDepartment(DepartmentDTO departmentDTO) throws DepartmentAlreadyExistsException {
        departmentRepository.findByDepartmentNameIgnoreCase(departmentDTO.getDepartmentName())
                .ifPresent(departmentEntity -> {
                    throw new DepartmentAlreadyExistsException("Department already exists with name " + departmentDTO.getDepartmentName());
                });
        departmentRepository.save(DepartmentMapper.mapToDepartmentEntity(departmentDTO, new DepartmentEntity()));
    }

    @Override
    public DepartmentDTO fetchDepartmentDetailsByDepartmentName(String departmentName, boolean fetchDetails) throws ResourceNotFoundException {
        DepartmentEntity departmentEntity = departmentRepository.findByDepartmentNameIgnoreCase(departmentName)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "departmentName", departmentName));
        DepartmentDTO departmentDTO = DepartmentMapper.mapToDepartmentDTO(departmentEntity, new DepartmentDTO());
        if (fetchDetails) {
            departmentDTO.setLecturers(
                    departmentEntity.getRegisteredLecturers().stream().map(
                            lecturerEntity -> LecturerMapper.mapLecturerEntityToLecturerDTO(lecturerEntity, new LecturerDTO(), new AddressDTO(), false)
                    ).toList()
            );
            departmentDTO.setOfferedCourses(
                    departmentEntity.getCourses().stream().map(
                            courseEntity -> CourseMapper.mapToCourseDTO(courseEntity, new CourseDTO(),false)
                    ).toList()
            );
        }
        return departmentDTO;
    }

    @Override
    public boolean updateDepartmentId(DepartmentDTO departmentDTO) throws DepartmentAlreadyExistsException, ResourceNotFoundException {
        // first try to find department by name
        boolean isUpdated = false;
        DepartmentEntity departmentEntity = departmentRepository.findByDepartmentNameIgnoreCase(departmentDTO.getDepartmentName())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "departmentName", departmentDTO.getDepartmentName()));
        DepartmentEntity updatedDepartment = DepartmentMapper.mapToDepartmentEntity(departmentDTO, departmentEntity);
        departmentRepository.save(updatedDepartment);
        isUpdated = true;
        return isUpdated;
    }

    @Override
    public boolean deleteDepartment(String departmentName) throws ResourceNotFoundException {
        DepartmentEntity department = departmentRepository.findByDepartmentNameIgnoreCase(departmentName).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", departmentName)
        );
        departmentRepository.deleteById(department.getDepartmentId());
        return true;
    }


    @Override
    public List<DepartmentDTO> getAllDepartments(int pageNo, boolean ascending) {
        Pageable pageable;
        if (ascending) {
            pageable = PageRequest.of(pageNo, pageSize, Sort.by("departmentName").ascending());
        } else {
            pageable = PageRequest.of(pageNo, pageSize, Sort.by("departmentName").descending());
        }
        List<DepartmentEntity> departmentEntities = departmentRepository.findAll(pageable).getContent();
        return departmentEntities.stream()
                .map(departmentEntity -> DepartmentMapper
                        .mapToDepartmentDTO(departmentEntity, new DepartmentDTO())).toList();
    }

    @Override
    public Long getPageCount() {
        return (departmentRepository.count() + pageSize - 1) / pageSize;
    }

}
