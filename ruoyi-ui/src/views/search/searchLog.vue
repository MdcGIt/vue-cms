<template>
  <div class="app-container">
    <el-row>
      <el-form :model="queryParams"
              ref="queryForm"
              :inline="true"
              class="el-form-search">
        <el-form-item prop="query">
          <el-input v-model="queryParams.query" size="small">
          </el-input>
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
    </el-row>
    <el-row class="mb8">
      <el-button type="danger"
                  icon="el-icon-delete"
                  size="mini"
                  plain
                  :disabled="selectedIds.length==0"
                  @click="handleDelete">删除</el-button>
    </el-row>
    <el-row>
      <el-table v-loading="loading"
                :data="logList"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection"
                        width="50"
                        align="center" />
        <el-table-column type="index"
                        label="序号"
                        align="center"
                        width="100" />
        <el-table-column label="搜索词"
                        align="left"
                        prop="word"
                        show-overflow-tooltip />
        <el-table-column label="IP"
                        align="left"
                        width="100"
                        prop="ip" />
        <el-table-column label="客户端类型"
                        align="left"
                        width="100"
                        prop="clientType" />
        <el-table-column label="来源"
                        align="left"
                        prop="referer"
                        show-overflow-tooltip />
        <el-table-column label="搜索时间"
                          align="center"
                          width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.logTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作"
                        align="center"
                        width="220" 
                        class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button 
              size="mini"
              type="text"
              icon="el-icon-plus"
              @click="handleToDictWord('WORD', scope.row)">添加为扩展词</el-button>
            <el-button 
              size="mini"
              type="text"
              icon="el-icon-plus"
              @click="handleToDictWord('STOP', scope.row)">添加为停用词</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadLogList"
      />
    </el-row>
  </div>
</template>
<style scoped>
</style>
<script>
import { getSearchLogs, deleteSearchLogs } from "@/api/search/searchlog";
import { addDictWord } from "@/api/search/dictword";
export default {
  name: "SearchWord",
  data () {
    return {
      loading: false,
      selectedIds: [],
      logList: [],
      total: 0,
      queryParams: {
        query: undefined,
        pageSize: 20,
        pageNo: 1
      }
    };
  },
  created () {
    this.loadLogList();
  },
  methods: {
    loadLogList () {
      this.loading = true;
      getSearchLogs(this.queryParams).then(response => {
        this.logList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    reset () {
      this.form = {};
    },
    handleQuery () {
      this.queryParams.pageNo = 1;
      this.loadLogList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.selectedIds = selection.map(item => item.logId);
    },
    handleDelete (row) {
      const logIds = row.logId ? [ row.logId ] : this.selectedIds;
      this.$modal.confirm('是否确认删除选中的搜索日志?').then(function() {
        return deleteSearchLogs(logIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.resetQuery ();
      }).catch(() => {});
    },
    handleToDictWord (wordType, row) {
      const form = { wordType: wordType, words: [ row.word ] }
      addDictWord(form).then(response => {
        this.$modal.msgSuccess(response.msg);
      });
    }
  }
};
</script>