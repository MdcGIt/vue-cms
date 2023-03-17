<template>
  <div class="app-container">
    <el-form :model="queryParams"
             ref="queryForm"
             :inline="true"
             label-width="68px"
             class="el-form-search">
      <el-form-item prop="type" style="padding:2px;">
        <el-input placeholder="类型" v-model="queryParams.type" size="small" />
      </el-form-item>
      <el-form-item prop="taskId" style="padding:2px;">
        <el-input placeholder="任务ID" v-model="queryParams.taskId" size="small" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary"
                   icon="el-icon-search"
                   size="small"
                   @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh"
                   size="small"
                   @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading"
              :data="taskList">
      <el-table-column label="类型"
                       align="center"
                       width="80"
                       prop="type" />
      <el-table-column label="任务ID"
                       align="left"
                       prop="taskId" />
      <el-table-column label="状态"
                       align="center"
                       width="120">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="进度"
                       align="center"
                       width="150"
                       prop="percent">
        <template slot-scope="scope">
          <span>{{ scope.row.percent }}%</span>
        </template>
      </el-table-column>
      <el-table-column label="错误信息"
                       align="center"
                       width="150">
        <template slot-scope="scope">
          <el-button v-if="hasErrMessages(scope.row)" @click="showErrMessages(scope.row)">查看</el-button>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="开始时间"
                       align="center"
                       prop="startTime"
                       width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间"
                       align="center"
                       prop="endTime"
                       width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作"
                        align="center"
                        width="300" 
                        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            v-if="scope.row.status=='READY'||scope.row.status=='RUNNING'"
            size="mini"
            type="text"
            icon="el-icon-stop"
            @click="handleStop(scope.row)">中止</el-button>
          <el-button size="mini"
                      type="text"
                      icon="el-icon-delete"
                      @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script>
import { MessageBox } from 'element-ui'
import { getAsyncTaskList, stopAsyncTask, delAsyncTask } from "@/api/contentcore/core";

export default {
  name: "CmsAsyncTaskList",
  data () {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 异步任务表格数据
      taskList: [],
      // 查询参数
      queryParams: {
        type: "",
        taskId: ""
      }
    };
  },
  created () {
    this.loadAsyncTaskList();
  },
  methods: {
    loadAsyncTaskList () {
      this.loading = true;
      getAsyncTaskList(this.queryParams.type).then(response => {
        this.taskList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    statusType(status) {
      if (status === 'INTERRUPTED') {
        return 'warning';
      } else if (status === 'FAILED') {
        return 'danger';
      } else if (status === 'SUCCESS') {
        return 'success';
      }
      return '';
    },
    hasErrMessages(row) {
      return row.errMessages && row.errMessages != null && row.errMessages.length > 0;
    },
    showErrMessages(row) {
      MessageBox.alert(row.errMessages.join("<br/>"), "错误信息", {
          dangerouslyUseHTMLString: true
        });
    },
    handleQuery () {
      this.loadAsyncTaskList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleStop (row) {
      stopAsyncTask([row.taskId]).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAsyncTaskList();
        }
      });
    },
    handleDelete (row) {
      delAsyncTask([row.taskId]).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadAsyncTaskList();
        }
      });
    }
  }
};
</script>