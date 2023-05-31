<template>
  <div class="dashboard-editor-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <user-info></user-info>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="24">
        <shortcut></shortcut>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <cms-site-visit-stat></cms-site-visit-stat>
      </el-col>
      <el-col :span="12">
        <cms-site-data-stat></cms-site-data-stat>
        <el-card shadow="hover" class="mt10">
          <div slot="header" class="clearfix">
            <span>捐赠支持</span>
          </div>
          <div class="body">
            <el-row :gutter="20">
              <el-col :span="12" style="text-align:center;">
                <div class="mb10">微信</div>
                <el-image
                  style="height: 200px"
                  :src="require('@/assets/images/wxpay.png')"
                  fit="cover">
                </el-image>
                <div style="color: #909399;font-size:12px;line-height: 30px;">
                  向 无弦(**仪) 捐赠
                </div>
              </el-col>
              <el-col :span="12" style="text-align:center;">
                <div class="mb10">支付宝</div>
                <el-image
                  style="height: 200px"
                  :src="require('@/assets/images/alipay.png')"
                  fit="cover">
                </el-image>
                <div style="color: #909399;font-size:12px;line-height: 30px;">
                  向 无弦(**仪) 捐赠
                </div>
              </el-col>
            </el-row>
            <div style="display: inline-block; height: 30px; line-height: 30px">你可以请作者喝杯咖啡表示鼓励^_^</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import SysUserInfo from '@/views/system/dashboard/userInfo'
import SysShortcut from '@/views/system/dashboard/shortcut'
import CmsSiteVisitStat from '@/views/cms/dashboard/siteVisitStat'
import CmsSiteDataStat from '@/views/cms/dashboard/siteDataStat'

export default {
  name: 'Index',
  components: {
    'user-info': SysUserInfo,
    'shortcut': SysShortcut,
    'cms-site-visit-stat': CmsSiteVisitStat,
    'cms-site-data-stat': CmsSiteDataStat
  },
  data() {
    return {
    }
  },
  created () {
  },
  methods: {

  }
}
</script>
<style lang="scss" scoped>
.dashboard-editor-container {
  padding: 10px;
  background-color: rgb(240, 242, 245);
  position: relative;

  .chart-wrapper {
    background: #fff;
    padding: 16px 16px 0;
    margin-bottom: 32px;
  }
}

@media (max-width:1024px) {
  .chart-wrapper {
    padding: 8px;
  }
}
</style>