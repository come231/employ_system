package com.hkd.service.impl;

import com.hkd.mapper.SupplementCardDao;
import com.hkd.pojo.SupplementCard;
import com.hkd.service.SupplementCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("supplementCardService")
public class SupplementCardServiceImpl implements SupplementCardService {
    
    @Autowired
    private SupplementCardDao supplementCardDao;
    
    @Override
    public Integer findAllCount() {
        return supplementCardDao.findAllCount();
    }
    
    @Override
    public List<SupplementCard> findAll(Integer offset, Integer pageSize) {
        return supplementCardDao.findAll(offset, pageSize);
    }
    
    @Override
    public SupplementCard findById(Integer supplementId) {
        return supplementCardDao.findById(supplementId);
    }
    
    @Override
    public Integer findCountByEmpId(Integer empId) {
        return supplementCardDao.findCountByEmpId(empId);
    }
    
    @Override
    public List<SupplementCard> findByEmpId(Integer empId, Integer offset, Integer pageSize) {
        return supplementCardDao.findByEmpId(empId, offset, pageSize);
    }
    
    @Override
    public Integer findCountByStatus(Byte approveStatus) {
        return supplementCardDao.findCountByStatus(approveStatus);
    }
    
    @Override
    public List<SupplementCard> findByApproveStatus(Byte approveStatus, Integer offset, Integer pageSize) {
        return supplementCardDao.findByApproveStatus(approveStatus, offset, pageSize);
    }
    
    @Override
    public Integer findCountByCheckType(Byte checkType) {
        return supplementCardDao.findCountByCheckType(checkType);
    }
    
    @Override
    public List<SupplementCard> findByCheckType(Byte checkType, Integer offset, Integer pageSize) {
        return supplementCardDao.findByCheckType(checkType, offset, pageSize);
    }
    
    @Override
    public int insert(SupplementCard supplementCard) {
        return supplementCardDao.insert(supplementCard);
    }
    
    @Override
    public int update(SupplementCard supplementCard) {
        return supplementCardDao.update(supplementCard);
    }
    
    @Override
    public int delete(Integer supplementId) {
        return supplementCardDao.delete(supplementId);
    }
    
    @Override
    public int updateApproveStatus(Integer supplementId, Byte approveStatus, String approveOpinion, Integer approverId) {
        return supplementCardDao.updateApproveStatus(supplementId, approveStatus, approveOpinion, approverId);
    }
}