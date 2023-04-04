<template>
  <div class="">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button 
          icon="el-icon-refresh"
          size="small"
          @click="handleQuery">{{ $t("Common.Refresh") }}</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading"
              :data="dataList">
      <el-table-column 
        label="广告名称"
        align="center"
        prop="adName" />
      <el-table-column 
        label="IP"
        align="center"
        prop="ip" />
      <el-table-column 
        label="区域"
        align="center"
        prop="address" />
      <el-table-column 
        label="来源"
        align="center"
        prop="referer" />
      <el-table-column 
        label="终端类型"
        align="center"
        prop="deviceType" />
      <el-table-column 
        label="浏览器类型"
        align="center"
        prop="browser" />
      <el-table-column 
        label="时间"
        align="center"
        prop="evtTime" />
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadAdClickLogList" />
  </div>
</template>
<style scoped>
</style>
<script>
import { getAdClickLogList  } from "@/api/advertisement/statistics";

export default {
  name: "CMSAdvertisementClickLog",
  data () {
    return {
      loading: false,
      total: 0,
      dataList: [],
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 50
      }
    };
  },
  created() {
    this.loadAdClickLogList();
  },
  methods: {
    loadAdClickLogList () {
      this.loading = true;
      getAdClickLogList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadAdClickLogList();
    },
    resetQuery () {
      this.queryParams.pageNum = 1;
      this.handleQuery();
    }
  }
};
</script>