package com.ruoyi.common.mybatisplus;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.utils.SpringUtils;

public interface IBackupService<T> extends IService<T> {
    
	BackupTableService BACKUP_SERVICE = SpringUtils.getBean(BackupTableService.class);

	default public void backup(T entity, Long backupId, String operator) {
		this.backup(entity, backupId, operator, null);
	}
	
	default public void backup(T entity, Long backupId, String operator, String backupRemark) {
		BACKUP_SERVICE.backup(entity, backupId, operator, backupRemark);
	}
	
	default public void recover(Long backupId, Class<?> entityClazz) {
		BACKUP_SERVICE.recover(backupId, entityClazz);
	}
}
