package com.ruoyi.contentcore.util;

import com.ruoyi.contentcore.core.IResourceType;
import com.ruoyi.contentcore.core.impl.ResourceType_File;

import java.util.List;

public class ResourceUtils {

    /**
     * 通过文件后缀名获取对应资源类型，由于文件资源包含所有文件类型，再获取到超过1个资源类型时将文件资源类型移除再获取。
     *
     * @param suffix
     * @return
     */
    public static IResourceType getResourceTypeBySuffix(String suffix) {
        List<IResourceType> list = ContentCoreUtils.getResourceTypes().stream().filter(rt -> rt.check(suffix)).toList();
        if (list.size() > 1) {
            return list.stream().filter(rt -> !rt.getId().equals(ResourceType_File.ID)).findFirst().get();
        }
        return list.get(0);
    }
}
