package com.ruoyi.common.mybatisplus.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.mybatisplus.service.impl.BackupTableService;
import com.ruoyi.common.utils.SpringUtils;

public interface IBackupService<T> extends IService<T> {
    
	BackupTableService BACKUP_SERVICE = SpringUtils.getBean(BackupTableService.class);

	default public void backup(T entity, String operator) {
		this.backup(entity, operator, null);
	}
	
	default public void backup(T entity, String operator, String backupRemark) {
		BACKUP_SERVICE.backup(entity, operator, backupRemark);
	}
	
	default public void recover(Long backupId, Class<?> entityClass) {
		BACKUP_SERVICE.recover(backupId, entityClass);
	}
	
	default public void deleteBackups(List<Long> backupIds, Class<?> entityClass) {
		BACKUP_SERVICE.deleteByBackupIds(backupIds, entityClass);
	}
}
