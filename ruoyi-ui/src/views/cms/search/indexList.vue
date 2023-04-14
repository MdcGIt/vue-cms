<template>
  <div class="app-container">
    <el-row :gutter="24">
      <el-col :span="12" class="mb12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              type="danger"
              icon="el-icon-delete"
              size="mini"
              :disabled="multiple"
              @click="handleDelete">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              icon="el-icon-refresh"
              size="mini"
              @click="handleRebuildIndex">重建全站索引</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="contentType">
            <el-select v-model="queryParams.contentType"
                        placeholder="内容类型"
                        clearable
                        style="width: 110px">
              <el-option v-for="ct in contentTypeOptions"
                          :key="ct.id"
                          :label="ct.name"
                          :value="ct.id" />
            </el-select>
          </el-form-item>
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" placeholder="请输入搜索词">
            </el-input>
          </el-form-item>
          <el-form-item prop="onlyTitle">
            <el-checkbox v-model="queryParams.onlyTitle" label="仅匹配标题" border></el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="el-icon-search"
                @click="handleQuery">搜索</el-button>
              <el-button 
                icon="el-icon-refresh"
                @click="resetQuery">重置</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-table v-loading="loading"
              :data="contentList"
              @selection-change="handleSelectionChange">
      <el-table-column type="selection"
                       width="55"
                       align="center" />
      <el-table-column label="ID"
                       align="center"
                       prop="contentId"
                       width="180" />
      <el-table-column label="栏目"
                       align="center"
                       width="180"
                       prop="catalogName" />
      <el-table-column label="标题"
                       align="left"
                       prop="title">
        <template slot-scope="scope">
          <span v-html="scope.row.title"></span>
        </template>
      </el-table-column>
      <el-table-column label="类型"
                        width="110"
                        align="center"
                        prop="contentType"
                        :formatter="contentTypeFormat" />
      <el-table-column label="状态"
                        width="110"
                        align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusFormat(scope.row, 'status') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间"
                       align="center"
                       prop="_publishDate"
                       width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row._publishDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间"
                       align="center"
                       prop="_createTime"
                       width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row._createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作"
                       align="center"
                       width="180" 
                       class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini"
                     type="text"
                     icon="el-icon-search"
                     @click="handleShowDetail(scope.row)">详情</el-button>
          <el-button size="mini"
                     type="text"
                     icon="el-icon-delete"
                     @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0"
                :total="total"
                :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize"
                @pagination="getList" />

    <!-- 添加或修改资源对话框 -->
    <el-dialog title="索引详情"
               :visible.sync="open"
               width="960px"
               append-to-body>
      <el-row class="data_row" v-for="(value, key, index) in showData" :key="index">
        <el-col :span="4" class="data_row_left">
          {{ key }}：
        </el-col>
        <el-col :span="20">
          <span v-if="isHtmlField(key)" v-html="value"></span>
          <span v-else>{{ value }}</span>
        </el-col>
      </el-row>
      <div slot="footer"
           class="dialog-footer">
        <el-button @click="cancel">关 闭</el-button>
      </div>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress title="索引任务" :open.sync="openProgress" :taskId="taskId" @close="handleProgressClose"></cms-progress>
  </div>
</template>
<script>
import CMSProgress from '@/views/components/Progress';
import { getContentTypes } from "@/api/contentcore/catalog";
import { getContentIndexList, deleteContentIndex, rebuildIndex } from "@/api/contentcore/search";

export default {
  name: "CMSIndexList",
  dicts: ['CMSContentStatus', 'CMSContentAttribute'],
  components: {
    'cms-progress': CMSProgress
  },
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      taskId: undefined,
      openProgress: false,
      // 总条数
      total: 0,
      // 发布通道表格数据
      contentTypeOptions: [],
      contentList: [],
      showData: {},
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      // 表单参数
      form: {}
    };
  },
  created () {
    getContentTypes().then(response => {
      this.contentTypeOptions = response.data;
      this.addContentType = this.contentTypeOptions[0].id;
    });
    this.getList();
  },
  methods: {
    getList () {
      this.loading = true;
      getContentIndexList(this.queryParams).then(response => {
        this.contentList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    isHtmlField (key) {
      return key == 'title' || key == 'fullText';
    },
    contentTypeFormat (row, column) {
      var hitValue = [];
      this.contentTypeOptions.forEach(ct => {
        if (ct.id == ('' + row.contentType)) {
          hitValue = ct.name;
          return;
        }
      });
      return hitValue;
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    statusFormat (row, column) {
      return this.selectDictLabel(this.dict.type.CMSContentStatus, row[column]);
    },
    statusTagType(status) {
      if (status == 40) {
        return "warning";
      } else if (status == 20 || status == 30) {
        return "success";
      } else if (status == 0) {
        return "info";
      }
      return "";
    },
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.contentId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleShowDetail(row) {
      this.showData = row;
      this.open = true;
    },
    cancel () {
      this.open = false;
    },
    handleDelete (row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.ids;
      this.$modal.confirm("是否确认删除？").then(function () {
        return deleteContentIndex(contentIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(function () { });
    },
    handleRebuildIndex() {
      rebuildIndex().then(response => {
        if (response.data && response.data != "") {
          this.taskId = response.data;
          this.openProgress = true;
        }
      });
    },
    handleProgressClose(resultStatus) {
      if (resultStatus == 'SUCCESS' || resultStatus == 'INTERRUPTED') {
        this.getList();
      }
    }
  }
};
</script>
<style scoped>
.data_row {
  line-height: 30px;
}
.data_row_left {
  font-weight: 600;
  text-align: right;
}
</style>